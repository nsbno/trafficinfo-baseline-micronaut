# TODO replace all <placeholders>
terraform {
  backend "s3" {
    key            = "trafficinfo-baseline-micronaut/main.tfstate"
    bucket         = "469515120670-terraform-state"
    dynamodb_table = "469515120670-terraform-state"
    acl            = "bucket-owner-full-control"
    encrypt        = "true"
    kms_key_id     = "arn:aws:kms:eu-west-1:469515120670:alias/469515120670-terraform-state-encryption-key"
    region         = "eu-west-1"
  }
}

provider "aws" {
  region              = "eu-west-1"
  allowed_account_ids = ["469515120670"]

  default_tags {
    tags = {
      terraform   = "true"
      environment = local.environment
      application = "${local.name_prefix}-${local.application_name}"
    }
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

locals {
  environment      = "dev"
  name_prefix      = "trafficinfo"
  application_name = "baseline-micronaut"
}

module "trafficinfo-baseline-micronaut" {
  source = "../template"

  name_prefix      = local.name_prefix
  application_name = local.application_name

  environment = local.environment
}
