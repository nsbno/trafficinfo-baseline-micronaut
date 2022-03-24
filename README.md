[![CircleCI](https://circleci.com/gh/nsbno/trafficinfo-baseline-micronaut.svg?style=svg&circle-token=67eb02d828d5a7e61e775f7840c387cc5be36dca)](https://circleci.com/gh/nsbno/trafficinfo-baseline-micronaut)

# Microservice baseline - based on Micronaut with Kotlin
This project serves as a baseline when you wish to create a new microservice. 

## Tech
- Latest Micronaut
- Latest Kotlin
- Latest Gradle

## Application Documentation
- [Development](/docs/development.md)
- [Installation](/docs/installation.md)
- [Configuration](/docs/configuration.md)
- [Operation](/docs/operation.md)

## Technical Requirements.
### MUST
- must contain JavaDoc for the most important classes containing the main app/business logic
- must contain JavaDoc for the most important methods containing the main app/business logic.
- must respond to /health endpoint with status 200 if UP.
- must send log to CloudWatch and to elasticsearch/kibana
- must send metrics to CloudWatch
- must use access token to authenticate user
- must use CloudWatch Alarms for to notify when something is wrong.
- must be deployed using deployment pipeline
- must be deployed using terraform code.

### MAY
- may use access token scopes to authorize user for fine grained access control.

## Infrastructure checklist for new microservice
### Create a new repository with "use this template" on Github.  
Clone repository and update all references to micronaut baseline with new project name
   1. gradle config
   2. source code, packages, classes etc.
   3. terraform code
   4. circleci config
   5. readme.md
 
### Add deployment pipeline 
   1. add statemachine for deployment pipeline.
   2. add ecr repo in service account for docker images.
