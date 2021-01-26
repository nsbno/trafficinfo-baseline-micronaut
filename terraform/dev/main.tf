# TODO replace all <placeholders>
terraform {
  required_version = "= 0.12.24"

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
  version             = "3.21.0"
  region              = "eu-west-1"
  allowed_account_ids = ["469515120670"]
}

provider "grafana" {
  url    = "https://grafana.test.common-services.vydev.io/"
  auth   = "eyJrIjoieHFiWDdLNkVPSU1JWmNoUVFURzBuNEZEZGRNRmY0b3UiLCJuIjoidHJhZmZpY2luZm8iLCJpZCI6MX0="
  org_id = 1
}

locals {
  name_prefix      = "trafficinfo"
  application_name = "baseline-micronaut"
  environment      = "dev"
  tags = {
    terraform   = "true"
    environment = local.environment
    application = "${local.name_prefix}-${local.application_name}"
  }
}

module "trafficinfo-baseline-micronaut" {
  environment          = local.environment
  source               = "../template"
  name_prefix          = local.name_prefix
  application_name     = local.application_name
  task_container_image = "latest"
  tags                 = local.tags
  grafana_create_dashboard = true
#  grafana_use_existing_folder = 53
#  grafana_folder_name = "Trafficinfo > Daniel Test"
}
