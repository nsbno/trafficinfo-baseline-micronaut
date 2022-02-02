data "aws_caller_identity" "current-account" {}
data "aws_region" "current" {}

locals {
  current_account_id = data.aws_caller_identity.current-account.account_id
  current_region     = data.aws_region.current.name
  service_account_id = "929368261477"
  shared_config      = jsondecode(data.aws_ssm_parameter.shared_config.value)

  # make resource server same in all envs. f.ex. services.trafficinfo.vydev.io
  cognito_base_url = "https://services.${trimprefix(local.shared_config.hosted_zone_name, "${var.environment}.")}"
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
module "cognito" {
  source = "github.com/nsbno/terraform-aws-central-cognito?ref=0.1.0"

  name_prefix = var.name_prefix
  application_name = var.application_name

  environment = var.environment

  resource_server_base_url = local.cognito_base_url
  resource_server_scopes   = {
    read_scope = {
      name = "read"
      description = "Read scope for the service"
    }
    write_scope = {
      name = "write"
      description = "Write scope for the service."
    }
    update_scope = {
      name = "update"
      description = "Update scope for the service."
    }
    delete_scope = {
      name = "delete"
      description = "Delete scope for the service."
    }
  }

  user_pool_client_scopes = [
    "${local.cognito_base_url}/${var.application_name}/read",
    "${local.cognito_base_url}/${var.application_name}/write",
    "${local.cognito_base_url}/${var.application_name}/update",
    "${local.cognito_base_url}/${var.application_name}/delete",
  ]
}

module "api_gateway" {
  source = "github.com/nsbno/terraform-aws-api-gateway?ref=0.1.0"

  name_prefix      = var.name_prefix
  application_name = var.application_name

  domain_name = "services.${local.shared_config.hosted_zone_name}"
  base_path   = var.application_name

  schema = templatefile("../static/openapi/driftstjenester-backend.yml", {
    hosted_zone_name = local.shared_config.hosted_zone_name
    base_path        = var.application_name
  })

  enable_xray = true
}

data "aws_ecr_repository" "this" {
  registry_id = local.service_account_id
  name        = "${var.name_prefix}-${var.application_name}"
}

module "service" {
  source = "github.com/nsbno/terraform-aws-ecs-service?ref=0.4.0"

  name_prefix = var.application_name

  vpc_id             = local.shared_config.vpc_id
  private_subnet_ids = local.shared_config.private_subnet_ids
  cluster_id         = local.shared_config.ecs_cluster_id

  application_container = {
    name     = "main"
    image    = "${data.aws_ecr_repository.this.repository_url}:${var.task_container_image}"
    port     = 8080
    protocol = "HTTP"
  }

  lb_listeners = [{
    listener_arn = local.shared_config.lb_listener_arn
    security_group_id = local.shared_config.lb_security_group_id
    path_pattern = "/${var.application_name}/*"
  }]

  lb_health_check = {
    path = "${var.application_name}/health"
  }
}


module "ecs-microservice" {
  source             = "github.com/nsbno/terraform-aws-trafficinfo?ref=d952ae1830215a98513089c8fa19c7307fee3b10/ecs-microservice"
  environment        = var.environment
  application-config = "" # Not being used by anything

  sqs_queues           = []
  sns_subscribe_topics = []
  s3_read_buckets      = []
  hosted_zone_name     = local.shared_config.hosted_zone_name

  ##################
  # PagerDuty endpoints for service to send alarms.
  pager_duty_critical_endpoint = var.pager_duty_critical_endpoint
  pager_duty_degraded_endpoint = var.pager_duty_degraded_endpoint

  enable_elasticcloud = true
  lambda_elasticcloud = local.shared_config.lambda_elasticsearch_alias

  # Enable generation of standard dashboards with ecs-microservice module.
  grafana_create_dashboard = true

  alarms_to_slack_function_name = ""
}
