data "aws_caller_identity" "current-account" {}
data "aws_region" "current" {}

locals {
  current_account_id = data.aws_caller_identity.current-account.account_id
  current_region     = data.aws_region.current.name
  service_account_id = "929368261477"
  shared_config      = jsondecode(data.aws_ssm_parameter.shared_config.value)

  # User pool in local cognito account to use when using a local cognito.
  user_pool_id = local.shared_config.user_pool_id

  # User pool in central cognito account to use when using a central cognito.
  cognito_central_user_pool_id = length(var.cognito_central_user_pool_id)>0 ? var.cognito_central_user_pool_id : local.shared_config.cognito_central_user_pool_id
  cognito_central_provider_arn = length(var.cognito_central_provider_arn)>0 ? var.cognito_central_provider_arn : local.shared_config.cognito_central_provider_arn
  cognito_central_resource_server_identifier = length(var.cognito_central_resource_server_identifier)>0 ? var.cognito_central_resource_server_identifier : local.shared_config.cognito_central_resource_server_identifier

  # For cognito configuration to Cognito
  # Toggle value used for provider and userpool by cognito_central_enable
  provider_arn = var.cognito_central_enable ? local.cognito_central_provider_arn : local.shared_config.user_pool_arn

  # make resource server same in all envs. f.ex. services.trafficinfo.vydev.io
  cognito_resource_server_identifier_base = "https://services.${trimprefix(local.shared_config.hosted_zone_name, "${var.environment}.")}"
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
  source             = "github.com/nsbno/terraform-aws-trafficinfo?ref=6adeed8/ecs-microservice"
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

    provider_arn = local.provider_arn
  })

  base_path   = var.application_name
  domain_name = "services.${local.shared_config.hosted_zone_name}"

  tags = var.tags

  sqs_queues           = []
  sns_subscribe_topics = []
  encryption_keys      = [aws_kms_key.baseline_params_key.arn]
  s3_read_buckets      = []
  alarms_degraded_sns_topic_arn = [local.shared_config.alarm_sns_topic_arn]
  alarms_critical_sns_topic_arn = [local.shared_config.alarm_sns_topic_arn]
  hosted_zone_name     = local.shared_config.hosted_zone_name

  ##################
  # Added Cognito configuration as example of how to configura a service
  # with authentication and authorization.
  #
  # Cognito user pool to create resources in.
  # This will be used to create resource servers in the local account cognito instance.
  user_pool_id = local.user_pool_id

  cognito_resource_server_identifier_base = local.cognito_resource_server_identifier_base

  # Enabled to create a resource server for the microservice in Cognito.
  create_resource_server = true

  # Enabled to create an app client for the microservice in Cognito..
  create_app_client = true

  # resource server scopes, just for testing.
  resource_server_scopes = local.resource_server_scopes

  # To generate a appclient, you need at least one scope for it.
  # Baseline has access to the Whoami Service, and also itself.
  app_client_scopes = local.app_client_scopes

  # this is the account id to cognito where client credentials
  # for the microservice are retrieved from secrets manager.
  cognito_central_account_id = var.cognito_central_account_id
  cognito_central_env        = var.cognito_central_override_env
  cognito_central_enable     = var.cognito_central_enable
  cognito_central_user_pool_id = local.cognito_central_user_pool_id
  cognito_central_resource_server_identifier = local.cognito_central_resource_server_identifier

  enable_elasticcloud = true
  lambda_elasticcloud = local.shared_config.lambda_elasticsearch_alias

  # Enable generation of standard dashboards with ecs-microservice module.
  grafana_create_dashboard = true

  # Enable x-ray tracing.
  api_gateway_enable_xray = true
}

resource "aws_kms_key" "baseline_params_key" {}
resource "aws_kms_alias" "baseline_params_key_alias" {
  target_key_id = aws_kms_key.baseline_params_key.key_id
  name          = "alias/${var.name_prefix}-${var.application_name}_params_key"
}
