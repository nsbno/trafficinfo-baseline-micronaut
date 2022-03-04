data "aws_region" "current" {}
data "aws_caller_identity" "current" {}

/*
 * == Shared Config
 *
 * Get parameters that have been configured from the `<team>-aws` repository.
 */
data "aws_ssm_parameter" "shared_config" {
  name = "/${var.name_prefix}/shared_application_config"
}

locals {
  shared_config      = jsondecode(data.aws_ssm_parameter.shared_config.value)
  service_account_id = "929368261477"
}

/*
 * == Required Services
 *
 * Various services that are required by the application
 */
locals {
  cognito_base_url = "services.${trimprefix(local.shared_config.hosted_zone_name, "${var.environment}.")}"
}

module "cognito" {
  source = "github.com/nsbno/terraform-aws-central-cognito?ref=0.2.1"

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

  user_pool_client_scopes = []
}

module "api_gateway" {
  source = "github.com/nsbno/terraform-aws-api-gateway?ref=0.1.0"

  name_prefix      = var.name_prefix
  application_name = var.application_name

  domain_name = "services.${local.shared_config.hosted_zone_name}"
  base_path   = var.application_name

  schema = templatefile("../static/openapi/baseline.yml", {
    hosted_zone_name   = local.shared_config.hosted_zone_name
    base_path          = var.application_name
    user_pool_id       = module.cognito.user_pool_id
    cognito_account_id = module.cognito.account_id
  })

  enable_xray = true
}

module "redis" {
  source = "github.com/nsbno/terraform-aws-redis?ref=0.1.0"

  application_name = var.application_name
  security_group_ids = [module.service.security_group_id]

  subnet_ids = local.shared_config.private_subnet_ids
}

resource "aws_cloudwatch_log_subscription_filter" "elasticcloud_logs" {
  destination_arn = local.shared_config.lambda_elasticsearch_alias
  filter_pattern  = ""

  log_group_name  = "${var.name_prefix}-${var.application_name}"
  name            = "ElasticsearchStream-${var.name_prefix}"
  distribution    = "ByLogStream"
}

/*
 * == Application
 */
module "service" {
  source = "github.com/nsbno/terraform-aws-ecs-service?ref=0.4.1"

  name_prefix = "${var.name_prefix}-${var.application_name}"

  vpc_id             = local.shared_config.vpc_id
  private_subnet_ids = local.shared_config.private_subnet_ids
  cluster_id         = local.shared_config.ecs_cluster_id

  application_container = {
    name     = "main"
    image    = "${local.shared_config.ecr_base_urn}/${var.name_prefix}-${var.application_name}:${var.application_image_tag}"
    port     = 8080
    protocol = "HTTP"
  }

  lb_listeners = [{
    listener_arn = nonsensitive(local.shared_config.lb_listener_arn)
    security_group_id = local.shared_config.lb_security_group_id
    path_pattern = "/${var.application_name}/*"
  }]

  lb_health_check = {
    path = "/${var.application_name}/health"
  }
}

module "service_permissions" {
  source = "github.com/nsbno/terraform-aws-service-permissions?ref=0.2.0"

  role_name = module.service.task_role_name

  ssm_parameters = [
    {
      // Allow to get the application config
      arn = "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/${local.application_config_path}*"
      permissions = ["get"]
    },
    {
      // Allow to get the application config
      arn = "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/${local.base_config_path}/application*"
      permissions = ["get"]
    },
  ]

  cloudwatch_metrics = [
    {
      arn = "*"
      permissions = ["put"]
    }
  ]

  // TODO: Put your required resources in here!
  //       Check the module's documentation for more info.
}

/*
 * == Monitoring and Alarms
 */
module "alarm_setup" {
  source = "github.com/nsbno/terraform-aws-alarm-setup?ref=0.1.0"

  name_prefix = var.application_name

  slack_lambda_name = "${var.name_prefix}-alarms-to-slack"
  pager_duty_endpoint_urls = {
    // TODO: Put your pager duty endpoints in here
    critical = ""
    degraded = ""
  }
}

module "grafana_dashboard" {
  source = "github.com/nsbno/terraform-grafana-service-dashboard?ref=0.1.0"

  name_prefix      = var.name_prefix
  application_name = var.application_name
  environment      = var.environment

  ecs_cluster = local.shared_config.ecs_cluster_name
}

module "alb_alarms" {
  source = "github.com/nsbno/terraform-aws-alarms//modules/alb?ref=0.1.0"

  name_prefix = module.service.log_group_name
  target_group_arn_suffix = module.service.target_group_arn_suffixes[0]
  load_balancer_arn_suffix = local.shared_config.lb_arn

  alarm_sns_topic_arns = [
    module.alarm_setup.critical_sns_arn
  ]
}

module "api_gateway_alarms" {
  source = "github.com/nsbno/terraform-aws-alarms//modules/api-gateway?ref=0.1.0"

  name_prefix = "${var.name_prefix}-${var.application_name}"
  api_name = module.api_gateway.rest_api_id

  alarm_sns_topic_arns = [
    module.alarm_setup.degraded_sns_arn
  ]
}

module "ecs_service_alarms" {
  source = "github.com/nsbno/terraform-aws-alarms//modules/ecs-service?ref=0.1.0"

  name_prefix = "${var.name_prefix}-${var.application_name}"
  ecs_cluster_name = local.shared_config.ecs_cluster_name
  ecs_service_name = var.application_name

  alarm_sns_topic_arns = [
    module.alarm_setup.critical_sns_arn
  ]
}

/*
 * == Config
 *
 * Share configuration for the application(s) to use
 */
locals {
  base_config_path = "${var.name_prefix}/config"
  application_config_path = "${local.base_config_path}/${var.application_name}"

  config_parameters = {
    "redis/uri" = {
      value = "rediss://${module.redis.primary_endpoint_address}"
    }
    "cognito/clientId" = {
      value = module.cognito.client_id
      secret = true
    }
    "cognito/clientSecret" = {
      value = module.cognito.client_secret
      secret = true
    }
    "jwksUrl" = {
      value = module.cognito.jwks_url
    }
    "cognito/url" = {
      value = module.cognito.auth_url
    }
  }
}

resource "aws_ssm_parameter" "configuration" {
  for_each = local.config_parameters

  name  = "/${local.application_config_path}/${each.key}"
  value = each.value.value

  type  = try(each.value.secret, false) ? "SecureString" : "String"
}
