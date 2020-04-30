data "aws_availability_zones" "main" {}
data "aws_caller_identity" "current-account" {}
data "aws_region" "current" {}

locals {
  current_account_id = data.aws_caller_identity.current-account.account_id
  current_region     = data.aws_region.current.name
  shared_config      = jsondecode(data.aws_ssm_parameter.shared_config.value)

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

##################################
#                                #
# Shared configuration           #
#                                #
##################################
data "aws_ssm_parameter" "shared_config" {
  name = "/trafficinfo/shared_application_config"
}



###################################
##                                #
## Microservice                   #
##                                #
###################################
#module "baseline-micronaut" {
#  source                          = "github.com/nsbno/terraform-aws-ecs-fargate?ref=06e632c"
#  name_prefix                     = "${var.name_prefix}-${var.application_name}"
#  vpc_id                          = local.shared_config.vpc_id
#  private_subnet_ids              = local.shared_config.private_subnet_ids
#  lb_arn                          = local.shared_config.lb_arn
#  cluster_id                      = local.shared_config.ecs_cluster_id
#  task_container_image            = "929368261477.dkr.ecr.eu-west-1.amazonaws.com/${var.name_prefix}-${var.application_name}:${data.aws_ssm_parameter.version.value}-SHA1"
#  task_container_port             = 8080
#  task_container_assign_public_ip = true
#  health_check = {
#    port = 8080
#    path = "/${var.application_name}/health"
#  }
#  health_check_grace_period_seconds = 30
#  tags                              = var.tags
#}
#
#resource "aws_lb_listener_rule" "this" {
#  listener_arn = local.shared_config.lb_listener_arn
#  priority     = 340
#
#  action {
#    type             = "forward"
#    target_group_arn = module.baseline-micronaut.target_group_arn
#  }
#
#  condition {
#    path_pattern {
#      values = ["/${var.application_name}/*"]
#    }
#  }
#}
#
#resource "aws_security_group_rule" "incoming" {
#  security_group_id        = module.baseline-micronaut.service_sg_id
#  type                     = "ingress"
#  protocol                 = "tcp"
#  from_port                = 8080
#  to_port                  = 8080
#  source_security_group_id = local.shared_config.lb_security_group_id
#}
#
#data "aws_ssm_parameter" "version" {
#  name = "/${var.name_prefix}/${var.name_prefix}-${var.application_name}"
#}
#
#
#resource "aws_iam_role_policy" "cloudwatch_to_microservice" {
#  policy = data.aws_iam_policy_document.cloudwatch_for_microservice.json
#  role   = module.baseline-micronaut.task_role_name
#}
#
#resource "aws_iam_role_policy" "ssm_to_microservice" {
#  policy = data.aws_iam_policy_document.ssm_for_microservice.json
#  role   = module.baseline-micronaut.task_role_name
#}
#
#
###################################
##                                #
## API Gateway                    #
##                                #
###################################
#locals {
#  api_gateway_schema = templatefile("../static/openapi/baseline.yml", {
#    hosted_zone_name = local.shared_config.hosted_zone_name
#    basePath         = var.application_name
#    provider_arn     = local.shared_config.user_pool_arn
#  })
#}
#resource "aws_api_gateway_rest_api" "api_gateway_microservice_rest_api" {
#  name = "${var.name_prefix}-${var.application_name}"
#  body = local.api_gateway_schema
#}
#
#resource "aws_api_gateway_deployment" "api_gateway_microservice_rest_api_deployment_v1" {
#  rest_api_id = aws_api_gateway_rest_api.api_gateway_microservice_rest_api.id
#  stage_name  = "v1"
#  variables = {
#    hash = sha256(local.api_gateway_schema)
#  }
#  lifecycle {
#    create_before_destroy = true
#  }
#}
#
#resource "aws_api_gateway_base_path_mapping" "gateway_base_path_mapping" {
#  api_id      = aws_api_gateway_rest_api.api_gateway_microservice_rest_api.id
#  stage_name  = aws_api_gateway_deployment.api_gateway_microservice_rest_api_deployment_v1.stage_name
#  domain_name = "services.${local.shared_config.hosted_zone_name}"
#  base_path   = var.application_name
#}
#
#
###################################
##                                #
## CloudWatch alarms              #
##                                #
###################################
#resource "aws_cloudwatch_metric_alarm" "service_unhealthy" {
#  metric_name         = "UnHealthyHostCount"
#  alarm_name          = "${var.name_prefix}-${var.application_name}-unhealthy"
#  comparison_operator = "GreaterThanOrEqualToThreshold"
#  evaluation_periods  = 1
#  threshold           = 1
#  namespace           = "AWS/ApplicationELB"
#  dimensions = {
#    TargetGroup  = module.baseline-micronaut.target_group_arn_suffix
#    LoadBalancer = local.shared_config.lb_arn_suffix
#  }
#  period            = 60
#  statistic         = "Average"
#  alarm_description = "${var.name_prefix}-${var.application_name} service has unhealthy targets"
#  tags              = var.tags
#  alarm_actions     = [local.shared_config.alarms_sns_topic_arn]
#  ok_actions        = [local.shared_config.alarms_sns_topic_arn]
#}
#
#
###################################
##                                #
## Cognito                        #
##                                #
###################################
#locals {
#  resource_server_scopes = {
#    read_scope = {
#      "scope_name" : "read"
#      "scope_description" : "read scope for the service"
#    }
#    write_scope = {
#      "scope_name" : "write"
#      "scope_description" : "write scope for the service."
#    }
#  }
#  app_client_scopes = [
#    "https://services.${local.shared_config.hosted_zone_name}/whoami/read",
#    "https://services.${local.shared_config.hosted_zone_name}/${var.application_name}/read"
#  ]
#}
#
#resource "aws_cognito_resource_server" "resource_server" {
#  identifier = "https://services.${local.shared_config.hosted_zone_name}/${var.application_name}"
#  name       = "${var.name_prefix}-${var.application_name}"
#
#  dynamic "scope" {
#    for_each = [for key, value in local.resource_server_scopes : {
#      scope_name        = value.scope_name
#      scope_description = value.scope_description
#    }]
#
#    content {
#      scope_name        = scope.value.scope_name
#      scope_description = scope.value.scope_description
#    }
#  }
#
#  user_pool_id = local.shared_config.user_pool_id
#}
#
#resource "aws_cognito_user_pool_client" "app_client" {
#  depends_on                           = [aws_cognito_resource_server.resource_server]
#  name                                 = "${var.name_prefix}-${var.application_name}-client"
#  user_pool_id                         = local.shared_config.user_pool_id
#  generate_secret                      = true
#  explicit_auth_flows                  = ["ADMIN_NO_SRP_AUTH"]
#  allowed_oauth_flows                  = ["client_credentials"]
#  allowed_oauth_scopes                 = local.app_client_scopes
#  allowed_oauth_flows_user_pool_client = true
#}
#
#resource "aws_ssm_parameter" "cognito-clientid" {
#  name      = "/${var.name_prefix}/config/${var.application_name}/cognito/clientId"
#  type      = "String"
#  value     = aws_cognito_user_pool_client.app_client.id
#  overwrite = true
#}
#
#resource "aws_ssm_parameter" "cognito-clientsecret" {
#  name      = "/${var.name_prefix}/config/${var.application_name}/cognito/clientSecret"
#  type      = "String"
#  value     = aws_cognito_user_pool_client.app_client.client_secret
#  overwrite = true
#}
#
#resource "aws_ssm_parameter" "cognito-url" {
#  name  = "/${var.name_prefix}/config/${var.application_name}/cognito/url"
#  type  = "String"
#  value = "https://auth.${local.shared_config.hosted_zone_name}"
#}
