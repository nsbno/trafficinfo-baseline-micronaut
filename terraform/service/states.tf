locals {
  state_definition = <<-EOF
{
  "Comment": "A Deployment Pipeline State Machine",
  "StartAt": "Deploy to Test and Stage",
  "States": {
    "Deploy to Test and Stage": {
      "Comment": "Deployment to Test and Stage done in Parallel",
      "Type": "Parallel",
      "Next": "Bump Versions in Prod",
      "ResultPath": "$.Result",
      "Branches": [
        {
          "StartAt": "Stagger Parallel Steps",
          "States": {
            "Stagger Parallel Steps": {
              "Comment": "A small wait step to prevent hitting AWS service limits",
              "Type": "Wait",
              "Seconds": 2,
              "Next": "Bump Versions in Test"
            },
            "Bump Versions in Test": {
              "Comment": "Update SSM parameters to latest SHA1 of ECR and latest S3 revision for Lambdas",
              "Type": "Task",
              "Resource": "arn:aws:states:::lambda:invoke",
              "Next": "Deploy Test",
              "ResultPath": "$.Result",
              "Parameters": {
                "FunctionName": "${local.shared_config.function_names.set_version}",
                "Payload": ${local.test_input_set_version}
              }
            },
            "Deploy Test": {
              "Type": "Task",
              "Resource": "arn:aws:states:::lambda:invoke.waitForTaskToken",
              "Parameters": {
                "FunctionName": "${local.shared_config.function_names.single_use_fargate_task}",
                "Payload": ${local.test_input_single_use_fargate_task}
              },
              "TimeoutSeconds": 3600,
              "End": true
            }
          }
        },
        {
          "StartAt": "Bump Versions in Stage",
          "States": {
            "Bump Versions in Stage": {
              "Comment": "Update SSM parameters to latest SHA1 of ECR and latest S3 revision for Lambdas",
              "Type": "Task",
              "Resource": "arn:aws:states:::lambda:invoke",
              "Next": "Deploy Stage",
              "ResultPath": "$.Result",
              "Parameters": {
                "FunctionName": "${local.shared_config.function_names.set_version}",
                "Payload": ${local.stage_input_set_version}
              }
            },
            "Deploy Stage": {
              "Type": "Task",
              "Resource": "arn:aws:states:::lambda:invoke.waitForTaskToken",
              "Parameters": {
                "FunctionName": "${local.shared_config.function_names.single_use_fargate_task}",
                "Payload": ${local.stage_input_single_use_fargate_task}
              },
              "TimeoutSeconds": 3600,
              "Next": "Integration Tests"
            },
            "Integration Tests": {
              "Type": "Wait",
              "Seconds": 7,
              "End": true
            }
          }
        },
        {
          "StartAt": "Stagger Parallel Steps II",
          "States": {
            "Stagger Parallel Steps II": {
              "Comment": "A small wait step to prevent hitting AWS service limits",
              "Type": "Wait",
              "Seconds": 4,
              "Next": "Bump Versions in Service"
            },
            "Bump Versions in Service": {
              "Comment": "Update SSM parameters to latest SHA1 of ECR and latest S3 revision for Lambdas",
              "Type": "Task",
              "Resource": "arn:aws:states:::lambda:invoke",
              "Next": "Deploy Service",
              "ResultPath": "$.Result",
              "Parameters": {
                "FunctionName": "${local.shared_config.function_names.set_version}",
                "Payload": ${local.service_input_set_version}
              }
            },
            "Deploy Service": {
              "Type": "Task",
              "Resource": "arn:aws:states:::lambda:invoke.waitForTaskToken",
              "Parameters": {
                "FunctionName": "${local.shared_config.function_names.single_use_fargate_task}",
                "Payload": ${local.service_input_single_use_fargate_task}
              },
              "TimeoutSeconds": 3600,
              "End": true
            }
          }
        }
      ]
    },
    "Bump Versions in Prod": {
      "Comment": "Update SSM parameters to latest SHA1 of ECR and latest S3 revision for Lambdas",
      "Type": "Task",
      "Resource": "arn:aws:states:::lambda:invoke",
      "Next": "Deploy Prod",
      "ResultPath": "$.Result",
      "Parameters": {
        "FunctionName": "${local.shared_config.function_names.set_version}",
        "Payload": ${local.prod_input_set_version}
      }
    },
    "Deploy Prod":{
      "Type": "Task",
      "Resource": "arn:aws:states:::lambda:invoke.waitForTaskToken",
      "Parameters": {
        "FunctionName": "${local.shared_config.function_names.single_use_fargate_task}",
        "Payload": ${local.prod_input_single_use_fargate_task}
      },
      "TimeoutSeconds": 3600,
      "Next": "Smoke Tests"
    },
    "Smoke Tests": {
      "Type": "Wait",
      "Seconds": 7,
      "End": true
    }
  }
}
EOF
}
