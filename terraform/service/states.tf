locals {
  state_definition = <<-EOF
{
  "Comment": "A Deployment Pipeline State Machine",
  "StartAt": "Deploy to Test and Stage",
  "States": {
    "Deploy to Test and Stage": {
      "Comment": "Deployment to Test and Stage done in Parallel",
      "Type": "Parallel",
      "Next": "Raise Errors",
      "ResultPath": "$.Result",
      "Branches": [
        {
          "StartAt": "Bump Versions in Test",
          "States": {
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
              "ResultPath": "$.Result",
              "Catch": [{
                "ErrorEquals": ["States.ALL"],
                "ResultPath": "$.errors",
                "Next": "Catch Test Errors"
              }],
              "Parameters": {
                "FunctionName": "${local.shared_config.function_names.single_use_fargate_task}",
                "Payload": ${local.test_input_single_use_fargate_task}
              },
              "TimeoutSeconds": 3600,
              "End": true
            },
            "Catch Test Errors": {
              "Type": "Pass",
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
              "ResultPath": "$.Result",
              "Catch": [{
                "ErrorEquals": ["States.ALL"],
                "ResultPath": "$.errors",
                "Next": "Catch Stage Errors"
              }],
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
            },
            "Catch Stage Errors": {
              "Type": "Pass",
              "End": true
            }
          }
        },
        {
          "StartAt": "Bump Versions in Service",
          "States": {
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
              "ResultPath": "$.Result",
              "Catch": [{
                "ErrorEquals": ["States.ALL"],
                "ResultPath": "$.errors",
                "Next": "Catch Service Errors"
              }],
              "Parameters": {
                "FunctionName": "${local.shared_config.function_names.single_use_fargate_task}",
                "Payload": ${local.service_input_single_use_fargate_task}
              },
              "TimeoutSeconds": 3600,
              "End": true
            },
            "Catch Service Errors": {
              "Type": "Pass",
              "End": true
            }
          }
        }
      ]
    },
    "Raise Errors": {
      "Comment": "Raise previously caught errors, if any",
      "Type": "Task",
      "ResultPath": "$.errors_found",
      "Resource": "arn:aws:states:::lambda:invoke.waitForTaskToken",
      "Parameters": {
        "FunctionName": "${local.shared_config.function_names.error_catcher}",
        "Payload":  {
          "token.$": "$$.Task.Token",
          "input.$": "$.Result"
        }
      },
      "TimeoutSeconds": 3600,
      "Next": "Bump Versions in Prod"
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
