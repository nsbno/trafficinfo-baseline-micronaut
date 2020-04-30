data "aws_iam_policy_document" "cloudwatch_for_microservice" {
  statement {
    effect = "Allow"
    resources = [
      "*"
    ]
    actions = [
      "cloudwatch:PutMetricData"
    ]
  }
}

data "aws_iam_policy_document" "ssm_for_microservice" {
  statement {
    effect = "Allow"
    actions = [
      "ssm:GetParameters",
      "ssm:GetParameter",
      "ssm:GetParameterHistory",
      "ssm:GetParametersByPath"
    ]
    resources = [
      "arn:aws:ssm:eu-west-1:${local.current_account_id}:parameter/${var.name_prefix}/config/*",
    ]
  }
}
