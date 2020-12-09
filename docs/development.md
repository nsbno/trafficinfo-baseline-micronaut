# Development
To test if the application is working and running, the application exposes two endpoint
* /health
* /secure
  * /trainroute
  * /health

To call the endpoints run the application locally and call them on `http://localhost:8080`
or if running in Docker, locally or in ECS, call them on the path `/baseline-micronaut`.

When running in the cloud environment use the Cognito service to retrieve an Access Token 
to use as Authentication to the service. When running locally use basic auth and the 
hard coded username and password found in the `UserAuthenticationService`-class.

## Pre-requisites
Description of everything needed to be installed to start developing the application.

## Run
### Gradle wrapper
Run `./gradlew run` and it will start the application.

### docker 
Build a new image standing in the root-project-folder and execute `docker build -t microservice -f Docker/Dockerfile .` 
to create an image with name microservice.

To start the container `docker run -p8080:8080 microservice` to start a container that listens on port 8080 on
the local machine. Go to `http://localhost:8080/health` to check after the container has started. 

### IDEA
Start the main class by right click on it, should start without any special configuration.

## Build
Run `./gradlew assemble` and it will package the application into a runnable jar. 
Output is in `build/libs/baseline.jar`


## Upgrade Micronaut to new versions
See official docs for breaking changes
https://docs.micronaut.io/latest/guide/index.html#breaks

### Micronaut 1.3 to 2.0
Most changes need to be done is to update dependency versions and renamed artifacts.
They have restructured packages and moved to new packages.

TODO: add examples of renamed packages.
Examples of dependencies that have been moved
* io.micronaut.aws:micronaut-aws-parameter-store

### Micronaut 2.0 to 2.1 
No known issues

### Micronaut 2.1 to 2.2
TODO anything found?

## Code Linting
Code linting is done with ktlint and a gradle plugin running the linting when building the code.
There should be a commit hook checking the code on pre-commit so that you cant commit code that 
does not satisfy linting rules. When detecting lint errors, auto-format the code to match ktlint 
rules with `gradle ktlintFormat` command and all the code in the repo will be auto-formatted. 

## Deploy
Dev is a rapid development environment that has no automated pipelines, it is maintained
with `terraform apply` by the developers.

The test, stage and prod environments has automated pipelines by Step Functions in AWS and
should not be manually be updated from developers laptops to avoid problems with local 
laptop settings and concurrent changes by developers.

### Manually in DEV
The development environment is manually maintained with terraform code. 
No automated pipelines runs against the environment to avoid deployments to step on each
others toes when committing changes on components at the same time.

You have two alternatives to update DEV, 
1) Update and run the terraform script for DEV to deploy, update the version tag of the container.
2) To just redeploy your microservice using the latest tag, go to the AWS Fargate console
   and find the service task and click the Kill button. 
   This will stop the current container and recreate it from the latest tag.

### Automated from feature branch in TEST for testing
Update and run the terraform script for TEST to deploy, update the version tag of the container.
Commit and run the automated pipeline to apply in TEST. 

### Automated from master by pipeline in test, stage and production.
When a new build is built on master branch, the CircleCI script triggers an automated deployment
by the Step Function Pipeline framework created by Team Infrastructure. It will automatically
deploy a new update current latest version in SSM for the service and trigger a new pipeline 
deployment in test, stage and production.
 
## Release
Merge your changes to the master branch and automated pipelines will take care of the rest.
test, stage and production will be updated with your changes.

## Branching
See our official team strategy in confluence
https://jico.nsb.no/confluence/display/TRAFFICINFO/Versioning+and+branching

## Tech
* [Kotlin](https://kotlinlang.org/)
* [Micronaut](https://micronaut.io)
* [JUnit](https://junit.org/junit5/)
* [KotlinTest](https://github.com/kotlintest/kotlintest)

