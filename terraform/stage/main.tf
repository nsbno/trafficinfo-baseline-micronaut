# TODO replace all <placeholders>
terraform {
  required_version = "= 0.12.24"

  backend "s3" {
    key            = "trafficinfo-baseline-micronaut/main.tfstate"
    bucket         = "800989198581-terraform-state"
    dynamodb_table = "800989198581-terraform-state"
    acl            = "bucket-owner-full-control"
    encrypt        = "true"
    kms_key_id     = "arn:aws:kms:eu-west-1:800989198581:alias/800989198581-terraform-state-encryption-key"
    region         = "eu-west-1"
  }
}

provider "aws" {
  version             = "3.26.0"
  region              = "eu-west-1"
  allowed_account_ids = ["800989198581"]
}

locals {
  name_prefix      = "trafficinfo"
  application_name = "baseline-micronaut"
  environment      = "stage"
  tags = {
    terraform   = "true"
    environment = local.environment
    application = "${local.name_prefix}-${local.application_name}"
  }

}
# Grafana API Token stored in Secrets Manager.
data "aws_secretsmanager_secret_version" "grafana" {
  secret_id = "grafana"
}

# needed by ecs-microservice module to create a Grafana Dashboard for microservice.
provider "grafana" {
  url    = jsondecode(data.aws_secretsmanager_secret_version.grafana.secret_string)["url"]
  auth   = jsondecode(data.aws_secretsmanager_secret_version.grafana.secret_string)["api_token"]
  org_id = jsondecode(data.aws_secretsmanager_secret_version.grafana.secret_string)["org_id"]
}

data "aws_ssm_parameter" "version" {
  name = "/${local.name_prefix}/${local.name_prefix}-${local.application_name}"
}

module "trafficinfo-baseline-micronaut" {
  source               = "../template"
  name_prefix          = local.name_prefix
  environment          = local.environment
  application_name     = local.application_name
  task_container_image = "${data.aws_ssm_parameter.version.value}-SHA1"
  tags                 = local.tags

  # The Stage Delegated Cognito
  # cognito_central_account_id   = "214014793664"
  # hard coded against the only delegated cognito env we have for the moment.
  cognito_central_account_id   = "231176028624"
  cognito_central_override_env = "test"
}
