data "aws_caller_identity" "current-account" {}
data "aws_region" "current" {}

locals {
  current_account_id = data.aws_caller_identity.current-account.account_id
  current_region     = data.aws_region.current.name
  service_account_id = "929368261477"
  shared_config      = jsondecode(data.aws_ssm_parameter.shared_config.value)

  cognito_resource_server_identifier_base = "https://services.${local.shared_config.hosted_zone_name}"
  resource_server_scopes = {
    read_scope = {
      "scope_name" : "read"
      "scope_description" : "read scope for the service"
    }
    write_scope = {
      "scope_name" : "write"
      "scope_description" : "write scope for the service."
    }
    update_scope = {
      "scope_name" : "update"
      "scope_description" : "Update scope for the service."
    }
   delete_scope = {
      "scope_name" : "delete"
      "scope_description" : "Delete scope for the service."
    }
  }

  app_client_scopes = [
    "${local.cognito_resource_server_identifier_base}/${var.application_name}/read",
    "${local.cognito_resource_server_identifier_base}/${var.application_name}/write",
    "${local.cognito_resource_server_identifier_base}/${var.application_name}/update",
    "${local.cognito_resource_server_identifier_base}/${var.application_name}/delete",
  ]
}

##################################
#                                #
# Shared configuration           #
#                                #
##################################
data "aws_ssm_parameter" "shared_config" {
  name = "/trafficinfo/shared_application_config"
}

##################################
#                                #
# Microservice                   #
#                                #
##################################
module "ecs-microservice" {
  source             = "github.com/nsbno/terraform-aws-trafficinfo?ref=d3149b6/ecs-microservice"
  environment        = var.environment
  application-config = "" # Not being used by anything
  ecs_cluster = {
    id   = local.shared_config.ecs_cluster_id
    name = local.shared_config.ecs_cluster_name
  }
  health_check_path    = "/${var.application_name}/health"
  health_check_port    = 8080
  name_prefix          = var.name_prefix
  current_account      = local.current_account_id
  service_name         = var.application_name
  service_port         = 8080
  task_container_image = "${local.service_account_id}.dkr.ecr.${local.current_region}.amazonaws.com/${var.name_prefix}-${var.application_name}:${var.task_container_image}"
  task_container_port  = 8080
  vpc = {
    private_subnet_ids = local.shared_config.private_subnet_ids
    vpc_id             = local.shared_config.vpc_id
  }
  alb = {
    arn               = local.shared_config.lb_arn
    arn_suffix        = local.shared_config.lb_arn_suffix
    security_group_id = local.shared_config.lb_security_group_id
  }
  alb_http_listener = {
    arn = local.shared_config.lb_listener_arn
  }
  alb_priority = 340

  schema = templatefile("../static/openapi/baseline.yml", {
    hosted_zone_name = local.shared_config.hosted_zone_name
    basePath         = var.application_name
    provider_arn     = local.shared_config.user_pool_arn
  })

  base_path   = var.application_name
  domain_name = "services.${local.shared_config.hosted_zone_name}"

  tags = var.tags

  sqs_queues           = []
  sns_subscribe_topics = []
  encryption_keys      = [aws_kms_key.baseline_params_key.arn]
  s3_read_buckets      = []
  alarms_sns_topic_arn = [local.shared_config.alarm_sns_topic_arn]
  hosted_zone_name     = local.shared_config.hosted_zone_name

  ##################
  # Added Cognito configuration as example of how to configura a service
  # with authentication and authorization.
  #
  # Cognito user pool to create resources in.
  user_pool_id = local.shared_config.user_pool_id

  cognito_resource_server_identifier_base = local.cognito_resource_server_identifier_base

  # Enabled to create a resource server for the microservice in Cognito.
  create_resource_server = 1

  # Enabled to create an app client for the microservice in Cognito..
  create_app_client = 1

  # resource server scopes, just for testing.
  resource_server_scopes = local.resource_server_scopes

  # To generate a appclient, you need at least one scope for it.
  # Baseline has access to the Whoami Service, and also itself.
  app_client_scopes = local.app_client_scopes

  # this is the account id to cognito where client credentials
  # for the microservice are retrieved from secrets manager.
  cognito_account_id = var.cognito_account_id
  cognito_env = var.cognito_override_env

  enable_elasticcloud = true
  lambda_elasticcloud = local.shared_config.lambda_elasticsearch_alias


  ## Configure Grafana Dashboard, just enable generation
  # and use default values for all the other
  # grafana_create_dashboard = true

  # grafana_template_file = "${path.module}/custom-dashboard.tpl" # OPTIONAL
  # grafana_folder_name = "Some Name" # OPTIONAL display name for folder.
  # grafana_use_existing_folder = 123 # OPTIONAL folder id found in grafana
}

resource "aws_kms_key" "baseline_params_key" {}
resource "aws_kms_alias" "baseline_params_key_alias" {
  target_key_id = aws_kms_key.baseline_params_key.key_id
  name          = "alias/${var.name_prefix}-${var.application_name}_params_key"
}


###########################################################
# Create Resource Server and App Client in the central Cognito
#
# We need to Create all the oauth configuration in the central
# cognito and migrate our services over to use it so that
# the OAuth tokens is valid between different team accounts,
# For example traffic-control, traffic-gui and info.
###########################################################

# upload delegated cognito config to S3 bucket.
# this will trigger the delegated cognito terraform pipeline and and apply the config.
resource "aws_s3_bucket_object" "delegated-cognito-config" {
  count = length(var.cognito_account_id)>0 ? 1 : 0
  bucket = var.cognito_bucket
  key    = "${var.environment}/${local.current_account_id}/${var.name_prefix}-${var.application_name}.json"
  acl    = "bucket-owner-full-control"

  ## TODO maybe pull this out to a template to do more advanced conditional logic.
  content = jsonencode({
    # Configure resource server.
    resource_server = {
      name_prefix = "${var.name_prefix}-${var.application_name}"
      identifier  = "${local.cognito_resource_server_identifier_base}/${var.application_name}"

      scopes = [for key, value in local.resource_server_scopes : {
        scope_name        = value.scope_name
        scope_description = value.scope_description
      }]
    }

    # Configure a user pool client
    # TODO. this should be conditionally toggled.
    user_pool_client = {
      name_prefix     = "${var.name_prefix}-${var.application_name}"
      generate_secret = true

      allowed_oauth_flows                  = ["client_credentials"]
      allowed_oauth_scopes                 = local.app_client_scopes
      allowed_oauth_flows_user_pool_client = true
    }
  })
  content_type = "application/json"
}

##
# Read Credentials from Secrets Manager and set in microservice SSM config.
# Store the md5 of the cognito config so that a change in md5/config
# Will trigger a new update on dependent resources.
#
# Using workaround using time_sleep for async pipeline in cognito to complete
# configuration of resource server and application client in delegated cognito.
# The sleep wait will only occur when the dependent S3 file is updated
# and during normal operation without changes it will not pause here.
resource "time_sleep" "wait_for_credentials" {
  count = length(var.cognito_account_id)>0 ? 1 : 0
  create_duration = "300s"

  triggers = {
    config_md5 = md5(aws_s3_bucket_object.delegated-cognito-config[0].content)
  }
}

# The client credentials that are stored in Central Cognito.
data "aws_secretsmanager_secret_version" "microservice_client_credentials" {
  depends_on = [aws_s3_bucket_object.delegated-cognito-config[0], time_sleep.wait_for_credentials[0]]
  count = length(var.cognito_account_id)>0 ? 1 : 0
  secret_id = "arn:aws:secretsmanager:eu-west-1:${var.cognito_account_id}:secret:${local.current_account_id}-${var.name_prefix}-${var.application_name}"
}

# Store client credentials from Central Cognito in SSM so that the microservice can read it.
resource "aws_ssm_parameter" "client_id" {
  count = length(var.cognito_account_id)>0 ? 1 : 0
  name      = "/${var.name_prefix}/config/${var.application_name}/client_id"
  type      = "SecureString"
  value     = jsondecode(data.aws_secretsmanager_secret_version.microservice_client_credentials[0].secret_string)["client_id"]
  tags      = merge(var.tags, {
    # store the md5 as a tag to establish a dependency to the wait_for_credentials resource
    config_md5: time_sleep.wait_for_credentials[0].triggers.config_md5
  })
  overwrite = true
}

# Store client credentials from Central Cognito in SSM so that the microservice can read it.
resource "aws_ssm_parameter" "client_secret" {
  count = length(var.cognito_account_id)>0 ? 1 : 0
  name      = "/${var.name_prefix}/config/${var.application_name}/client_secret"
  type      = "SecureString"
  value     =  jsondecode(data.aws_secretsmanager_secret_version.microservice_client_credentials[0].secret_string)["client_secret"]
  tags      = merge(var.tags, {
    # store the md5 as a tag to establish a dependency to the wait_for_credentials resource
    config_md5: time_sleep.wait_for_credentials[0].triggers.config_md5
  })
  overwrite = true
}
