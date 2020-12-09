[![CircleCI](https://circleci.com/gh/nsbno/trafficinfo-baseline-micronaut.svg?style=svg&circle-token=67eb02d828d5a7e61e775f7840c387cc5be36dca)](https://circleci.com/gh/nsbno/trafficinfo-baseline-micronaut)

# Semi lightweight baseline - based on Micronaut with Kotlin
This project serves as a baseline when you wish to begin a new micro service exposing HTTP endpoints using Micronaut.

# System Documentation
See https://jico.nsb.no/confluence/display/TRAFFICINFO/Baseline+Micronaut

# Application Documentation
- [Development](/docs/development.md)
- [Installation](/docs/installation.md)
- [Configuration](/docs/configuration.md)
- [Operation](/docs/operation.md)

# Technical Requirements to microservices.
- must contain JavaDoc for the most important classes containing the main app/business logic
- must contain JavaDoc for the most important methods containing the main app/business logic.
- must respond to /health endpoint with status 200 if UP.
- must send log to CloudWatch and to elasticsearch/kibana
- must send metrics to CloudWatch
- must use access token to authenticate user
- must use CloudWatch Alarms for to notify when something is wrong.
- must be deployed using deployment pipeline
- must be deployed using terraform code.
- may use access token scopes to authorize user for fine grained access control.

# Infrastructure checklist for new microservice
1. Create a new repository with "use this template" on Github.  
    1. Give access to team "trafficinfo". Remove personal permission.
    2. Set up autolink in Github, prefix TRAFFICINFO-, URL https://jico.nsb.no/jira/browse/TRAFFICINFO-<num>
    
2. Clone repository and change what needs to be changed in .circleci/config.yml, Docker/Dockerfile and 
   Docker/runapp.sh. Change archive file name in build.gradle.kts. 

3. Set up an ECR for the microservice
Create a new module based on ecr-baseline-micronaut in trafficinfo-aws/terraform/circleci-init/main.tf.
Add the new ecr-repository to the module "ci_machine_user", and update the number of allowed ecr_count. 
Add a state machine for this project to the state_machine_arns list in the locals section.

4. CircleCI setup
CircleCI is responsible for running tests, and building the artifacts before publishing it to ECR to be deployed to ECS.
To do this it needs API keys and secrets to integrate with AWS and an endpoint in ECR to publish the docker image to. 
After pushing the new repo to github, a job should be automatically crated in CircleCI. Configure the newly 
created job with the following environment variables:
ECR_ENDPOINT
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
    
    The ECR endpoint can be found in the ECR configuration panel after it has been terraformed, should only include the hostname, not the path. 
    The AWS variables can be found in the AWS Systems Manager -> Parameter Store. AWS_ACCESS_KEY_ID can be found in ci-machine-user-id, and the 
    AWS_SECRET_ACCESS_KEY can be found under ci-machine-user-key.
 
    1. Exchange the "status badge" at the top of the readme by editing the following with the correct values for this project:
    \[\!\[CircleCI\](https://circleci.com/gh/nsbno/<PROJECT_NAME>.svg?style=svg&circle-token=<YOUR_STATUS_API_TOKEN>)\](https://circleci.com/gh/nsbno/<PROJECT_NAME>)
        * PROJECT_NAME is the name of the github project
        * YOUR_STATUS_API_TOKEN is found in the circle-ci project settings. Go to "API Permissions" and create an API token with the scope "status".

    2. In CircleCI, Add the slack webhook-url to Circle CI for build notifications by going to project settings and "Slack Integration"

5. Update with correct project naming in all the terraform/\<ENVIRONMENT>/main.tf files.

test
