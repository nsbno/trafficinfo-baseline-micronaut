# TODO replace all <placeholders>
terraform {
  required_version = "0.12.24"

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
  version             = "2.58.0"
  region              = "eu-west-1"
  allowed_account_ids = ["336207361115"]
}

locals {
  name_prefix      = "trafficinfo"
  application_name = "baseline-micronaut"
}

module "trafficinfo-baseline-micronaut" {
  source           = "../template"
  name_prefix      = local.name_prefix
  application_name = local.application_name
  tags = {
    terraform   = "true"
    environment = "prod"
    application = "${local.name_prefix}-${local.application_name}"
  }
}
