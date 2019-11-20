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

# Infrastructure checklist for new microservice
1. Fork this repo from Github and change what needs to be changed in .circleci/config.yml, Docker/Dockerfile and 
Docker/runapp.sh.

2. Set up an ECR for the microservice
Create a new module based on ecr-baseline-micronaut in trafficinfo-aws/terraform/circleci-init/main.tf.

3. CircleCI setup
CircleCI is responsible for running tests, and building the artifacts before publishing it to ECR to be deployed to ECS.
To do this it needs API keys and secrets to integrate with AWS and an endpoint in ECR to publish the docker image to. 
After pushing the new repo to github, a job should be automatically crated in CircleCI. Configure the newly 
created job with the following environment variables:
ECR_ENDPOINT
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY

The ECR endpoint can be found in the ECR configuration panel after it has been terraformed. The AWS variables can be
found in the AWS Systems Manager -> Parameter Store. AWS_ACCESS_ID can be found in ci-machine-user-id, and the 
AWS_SECRET_ACCESS_KEY can be found under ci-machine-user-key.
 
4. Generate a "status badge" in Circle CI that can be used in README.md for the new microservice. 

5. Add the slack webhook-url to Circle CI for build notifications

6. WIP Create terraform stuff for new service
## Deploy til test
template/main.tf -> ECS

aws_security group_rule

add aws_ssm_param

expand trafficinfo module

pipeline-terraform.yml
