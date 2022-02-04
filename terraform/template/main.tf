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
}

/*
 * == Required Services
 *
 * Various services that are required by the application
 */
locals {
  cognito_base_url = "https://services.${trimprefix(local.shared_config.hosted_zone_name, "${var.environment}.")}"
}

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

module "redis" {
  source = "github.com/nsbno/terraform-aws-redis?ref=0.1.0"

  application_name = var.application_name
  security_group_ids = [module.service.security_group_id]

  subnet_ids = local.shared_config.private_subnet_ids
}

/*
 * == Application
 */
data "aws_ecr_repository" "this" {
  registry_id = local.shared_config.service_account_id
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

module "service_permissions" {
  source = "github.com/nsbno/terraform-aws-service-permissions?ref=0.1.0"

  task_role = module.service.task_role_id

  ssm_parameters = [
    {
      // Allow to get the application config
      arn = "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/${local.config_path}/*"
      permissions = ["get"]
    }
  ]

  // TODO: Put your required resources in here!
  //       Check the module's documentation for more info.
}

/*
 * == Monitoring and Alarms
 */
module "grafana_dashboard" {
  source = "github.com/nsbno/terraform-aws-grafana-dashboard?ref=0.1.0"

  name_prefix      = var.name_prefix
  application_name = var.application_name
  environment      = var.environment

  ecs_cluster = local.shared_config.ecs_cluster_name
  rds_instances = local.shared_config.rds_instance_id
  elasticache_group = module.redis.id
}

module "alarms" {
  source = "github.com/nsbno/terraform-aws-service-alarms?ref=0.1.0"

  load_balancers = []
  target_groups = []
  ecs_cluster = ""
  api_gateway = ""
}

module "alarms_to_slack" {
  source = "github.com/nsbno/terraform-aws-cloudwatch-slack-alarms?ref=0.0.1"
}

module "alarms_to_pager_duty" {
  source = "github.com/nsbno/terraform-aws-cloudwatch-pager-duty-alarms?ref=0.0.1"
}

module "logs_to_elasticcloud" {
  source = "github.com/nsbno/terraform-aws-elasticcloud?ref=0.0.1/modules/send_logs"

  log_group_name = module.service.log_group_name
  lambda_alias = local.shared_config.lambda_elasticsearch_alias
}

/*
 * == Config
 *
 * Share configuration for the application(s) to use
 */
locals {
  config_path = "/config/${var.application_name}"

  config_parameters = {
    "/redis/uri" = {
      value = "rediss://${module.redis.primary_endpoint_address}"
    }
    "/cognito/clientId" = {
      value = module.cognito.client_id
      secret = true
    }
    "/cognito/clientSecret" = {
      value = module.cognito.client_secret
      secret = true
    }
    "/cognito/jwksUrl" = {
      value = module.cognito.jwks_url
    }
    "/cognito/url" = {
      value = "https://auth.${local.shared_config.hosted_zone_name}"
    }
  }
}

resource "aws_ssm_parameter" "configuration" {
  for_each = local.config_parameters

  name  = "${local.config_path}${each.key}"
  value = each.value.value

  type  = try(each.value.secret, false) ? "SecureString" : "String"
}
