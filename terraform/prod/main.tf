# TODO replace all <placeholders>
terraform {
  required_version = ">=0.13.6"

  backend "s3" {
    key            = "trafficinfo-baseline-micronaut/main.tfstate"
    bucket         = "336207361115-terraform-state"
    dynamodb_table = "336207361115-terraform-state"
    acl            = "bucket-owner-full-control"
    encrypt        = "true"
    kms_key_id     = "arn:aws:kms:eu-west-1:336207361115:alias/336207361115-terraform-state-encryption-key"
    region         = "eu-west-1"
  }
}

provider "aws" {
  region              = "eu-west-1"
  allowed_account_ids = ["336207361115"]
}

locals {
  name_prefix      = "trafficinfo"
  application_name = "baseline-micronaut"
  environment      = "prod"
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
  environment          = local.environment
  name_prefix          = local.name_prefix
  application_name     = local.application_name
  task_container_image = "${data.aws_ssm_parameter.version.value}-SHA1"
  tags                 = local.tags

  # The Delegated Cognito Prod environment
  # cognito_central_account_id   = "387958190215"
  # hard coded against the only delegated cognito env we have for the moment.
  cognito_central_account_id   = "231176028624"
  cognito_central_override_env = "test"
}
