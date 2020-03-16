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
1. Create a new repository with "use this template" on Github.  
    1. Give access to team "trafficinfo". Remove personal permission.
    2. Set up autolink in Github, prefix TRAFFICINFO-, URL https://jico.nsb.no/jira/browse/TRAFFICINFO-<num>
    
2. Clone repository and change what needs to be changed in .circleci/config.yml, Docker/Dockerfile and 
   Docker/runapp.sh. Change archive file name in build.gradle.kts. 

3. Set up an ECR for the microservice
Create a new module based on ecr-baseline-micronaut in trafficinfo-aws/terraform/circleci-init/main.tf.
Add the new ecr-repository to the module "ci_machine_user", and update the number of allowed ecr_count. 

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
 
    1. Generate a "status badge" in Circle CI that can be used in README.md for the new microservice. If the project is private (default) you'll need to create
an access token first. This is currently only available in the old UI. 

    2. In CircleCI, Add the slack webhook-url to Circle CI for build notifications. This is currently only available in the old UI.

5. Establish OpenAPI schema by copying "empty.yml" (if a REST API is supposed to be exposed by the service)

6. Create application config in terraform/modules/application-config. Create files variables.tf, main.tf and output.tf

7. Create ECS module by copying one of the existing modules, e.g. svc-otc.yml and create a config containing at least application config and 
an ecs-microservice module. Remove all redundant config. NOTE: Remember to choose a unique ALB priority for the module. 

