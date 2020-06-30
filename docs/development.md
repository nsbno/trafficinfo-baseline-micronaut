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

## Migrate Micronaut 1.3 to 2.0
TODO add description 

### Code Linting
Code linting is done with ktlint and a gradle plugin running the linting when building the code.
There should be a commit hook checking the code on pre-commit so that you cant commit code that 
does not satisfy linting rules. When detecting lint errors, auto-format the code to match ktlint 
rules with `gradle ktlintFormat` command and all the code in the repo will be auto-formatted. 

## Deploy
Describe how this application is deployed in dev, test and production env.

## Release
When building for release (CircleCI), supply a parameter version on the commandline as so: 
`./gradlew assemble -Dversion=<releaseversion or hash>`

## Branching
Describe how the branching strategy is handled when developing this app.

## Tech

* JDK12
* [Micronaut](https://micronaut.io)
* [JUnit](https://junit.org/junit5/)
* [KotlinTest](https://github.com/kotlintest/kotlintest)

