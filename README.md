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
    2. Set up autolink in Github, prefix RP-, URL https://jico.nsb.no/jira/browse/RP-<num>
    
2. Clone repository and update all references to micronaut baseline with new project name
   1. gradle config
   2. source code, packages, classes etc.
   3. terraform code
   4. circleci config
   5. readme.md
 
3. Add microservice to trafficinfo-aws repository.
   1. add statemachine for deployment pipeline.
   2. add ecr repo in service account for docker images.