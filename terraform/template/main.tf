data "aws_availability_zones" "main" {}
data "aws_caller_identity" "current-account" {}
data "aws_region" "current" {}

locals {
  current_account_id = data.aws_caller_identity.current-account.account_id
  current_region     = data.aws_region.current.name
  service_account_id = "929368261477"
  shared_config      = jsondecode(data.aws_ssm_parameter.shared_config.value)
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
  source             = "github.com/nsbno/terraform-aws-trafficinfo?ref=TRAFFICINFO-487.central.cognito/ecs-microservice"
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

  cognito_resource_server_identifier_base = "https://services.${local.shared_config.hosted_zone_name}"

  # Enabled to create a resource server for the microservice in Cognito.
  create_resource_server = 1

  # Enabled to create an app client for the microservice in Cognito..
  create_app_client = 1

  # resource server scopes, just for testing.
  resource_server_scopes = {
    read_scope = {
      "scope_name" : "read"
      "scope_description" : "read scope for the service"
    }
    write_scope = {
      "scope_name" : "write"
      "scope_description" : "write scope for the service."
    }
  }

  # To generate a appclient, you need at least one scope for it.
  # Baseline has access to the Whoami Service, and also itself.
  app_client_scopes = [
    "https://services.${local.shared_config.hosted_zone_name}/whoami/read",
    "https://services.${local.shared_config.hosted_zone_name}/${var.application_name}/read"
  ]

  enable_elasticcloud = true
  lambda_elasticcloud = local.shared_config.lambda_elasticsearch_alias
}

# TODO: Resources from `trafficinfo-aws/terraform/modules/template/{kernel-kms.tf,svc-baseline.tf}`
resource "aws_ssm_parameter" "testvariable" {
  name      = "/${var.name_prefix}/config/${var.application_name}/testvariable"
  type      = "String"
  value     = "test"
  overwrite = true
}

resource "aws_kms_key" "baseline_params_key" {}
resource "aws_kms_alias" "baseline_params_key_alias" {
  target_key_id = aws_kms_key.baseline_params_key.key_id
  name          = "alias/${var.name_prefix}-${var.application_name}_params_key"
}

resource "grafana_folder" "collection" {
  title = title("${var.name_prefix} ${var.application_name}")
}

resource "grafana_dashboard" "dashboard_in_folder" {
  folder = grafana_folder.collection.id
  config_json = templatefile("../static/grafana/dashboard.tpl", {
    "name": title("${var.application_name} ${var.environment}")
    "environment": var.environment
    "uuid": filemd5("../static/grafana/dashboard.tpl")
  })
}