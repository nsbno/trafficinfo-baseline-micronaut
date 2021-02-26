# TODO replace all <placeholders>
terraform {
  required_version = "=0.13.6"

  backend "s3" {
    key            = "trafficinfo-baseline-micronaut/main.tfstate"
    bucket         = "929368261477-terraform-state"
    dynamodb_table = "929368261477-terraform-state"
    acl            = "bucket-owner-full-control"
    encrypt        = "true"
    kms_key_id     = "arn:aws:kms:eu-west-1:929368261477:alias/929368261477-terraform-state-encryption-key"
    region         = "eu-west-1"
  }
}

provider "archive" {
  version = "~> 2.0"
}

provider "aws" {
  version             = "3.27.0"
  region              = "eu-west-1"
  allowed_account_ids = ["929368261477"]
}

data "aws_caller_identity" "current-account" {}
data "aws_region" "current" {}

locals {
  current_account_id = data.aws_caller_identity.current-account.account_id
  current_region     = data.aws_region.current.name
  application_name   = "baseline-micronaut"
  name_prefix        = "${local.shared_config.name_prefix}-${local.application_name}"
  tags = {
    terraform   = "true"
    environment = "service"
    application = local.name_prefix
  }
  service_account_id = "929368261477"
  dev_account_id     = "469515120670"
  test_account_id    = "535719329059"
  stage_account_id   = "800989198581"
  prod_account_id    = "336207361115"
  trusted_accounts = [
    local.current_account_id,
    local.dev_account_id,
    local.test_account_id,
    local.stage_account_id,
    local.prod_account_id,
  ]
  shared_config = jsondecode(data.aws_ssm_parameter.shared_config.value)
}

##################################
#                                #
# Shared configuration           #
#                                #
##################################
data "aws_ssm_parameter" "shared_config" {
  name = "/trafficinfo/shared_config"
}

data "aws_s3_bucket" "project_bucket" {
  bucket = local.shared_config.project_bucket_id
}

##################################
#                                #
# Step Function                  #
#                                #
##################################
resource "aws_sfn_state_machine" "state_machine" {
  definition = local.state_definition
  name       = "${local.name_prefix}-state-machine"
  role_arn   = aws_iam_role.state_machine_role.arn
  tags       = local.tags
}

resource "aws_iam_role" "state_machine_role" {
  assume_role_policy = data.aws_iam_policy_document.state_machine_assume.json
  tags               = local.tags
}

resource "aws_iam_role_policy" "lambda_to_state_machine" {
  policy = data.aws_iam_policy_document.lambda_for_state_machine.json
  role   = aws_iam_role.state_machine_role.id
}

##################################
#                                #
# set-version Lambda             #
#                                #
##################################
# Inputs for set-version Lambda
locals {
  common_input_set_version = {
    role_to_assume        = local.shared_config.role_arns.set_version
    ecr_image_tag_filters = ["master-branch"]
    ecr_repositories      = [local.name_prefix]
    ssm_prefix            = local.shared_config.name_prefix
  }

  service_input_set_version = jsonencode(local.common_input_set_version)
  test_input_set_version = jsonencode(merge(local.common_input_set_version, {
    account_id = local.test_account_id
  }))
  stage_input_set_version = jsonencode(merge(local.common_input_set_version, {
    account_id = local.stage_account_id
  }))
  prod_input_set_version = jsonencode(merge(local.common_input_set_version, {
    account_id = local.prod_account_id
  }))
}

##################################
#                                #
# single-use-fargate-task Lambda #
#                                #
##################################
# Inputs for single-use-fargate-task Lambda
locals {
  string_templates_single_use_fargate_task = {
    cmd_to_run = "temp_role=$(aws sts assume-role --role-arn %s --role-session-name deployment-from-service-account) && export AWS_ACCESS_KEY_ID=$(echo $temp_role | jq -r .Credentials.AccessKeyId) && export AWS_SECRET_ACCESS_KEY=$(echo $temp_role | jq -r .Credentials.SecretAccessKey) && export AWS_SESSION_TOKEN=$(echo $temp_role | jq -r .Credentials.SessionToken) && %s",
  }
  common_input_single_use_fargate_task = {
    task_execution_role_arn = local.shared_config.role_arns.single_use_fargate_task_task_execution
    ecs_cluster             = local.shared_config.ecs_cluster_name
    subnets                 = local.shared_config.subnets
    "content.$"             = "$.content"
    "token.$"               = "$$.Task.Token",
    "state.$"               = "$$.State.Name"
    "state_machine_id"      = "${local.name_prefix}-state-machine"
  }

  service_input_single_use_fargate_task = jsonencode(merge(local.common_input_single_use_fargate_task, {
    task_role_arn = local.shared_config.role_arns.single_use_fargate_task_task
    image         = "vydev/terraform:0.13.6"
    cmd_to_run = format(lookup(local.string_templates_single_use_fargate_task, "cmd_to_run", ""),
      local.shared_config.role_arns.deploy_service,
      "cd terraform/service && terraform init -lock-timeout=120s && terraform apply -auto-approve -lock-timeout=120s"
    )
  }))
  test_input_single_use_fargate_task = jsonencode(merge(local.common_input_single_use_fargate_task, {
    task_role_arn = local.shared_config.role_arns.single_use_fargate_task_task
    image         = "vydev/terraform:0.13.6"
    cmd_to_run = format(lookup(local.string_templates_single_use_fargate_task, "cmd_to_run", ""),
      local.shared_config.role_arns.deploy_test,
      "cd terraform/test && terraform init -lock-timeout=120s && terraform apply -auto-approve -lock-timeout=120s"
    )
  }))
  stage_input_single_use_fargate_task = jsonencode(merge(local.common_input_single_use_fargate_task, {
    task_role_arn = local.shared_config.role_arns.single_use_fargate_task_task
    image         = "vydev/terraform:0.13.6"
    cmd_to_run = format(lookup(local.string_templates_single_use_fargate_task, "cmd_to_run", ""),
      local.shared_config.role_arns.deploy_stage,
      "cd terraform/stage && terraform init -lock-timeout=120s && terraform apply -auto-approve -lock-timeout=120s"
    )
  }))
  prod_input_single_use_fargate_task = jsonencode(merge(local.common_input_single_use_fargate_task, {
    task_role_arn = local.shared_config.role_arns.single_use_fargate_task_task
    image         = "vydev/terraform:0.13.6"
    cmd_to_run = format(lookup(local.string_templates_single_use_fargate_task, "cmd_to_run", ""),
      local.shared_config.role_arns.deploy_prod,
      "cd terraform/prod && terraform init -lock-timeout=120s && terraform apply -auto-approve -lock-timeout=120s"
    )
  }))
}
