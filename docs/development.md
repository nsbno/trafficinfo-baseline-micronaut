# Development

## Pre-requisites
Description of everything needed to be installed to start developing the application.

## Run
### maven
Run `./gradlew run` and it will start the application.

### docker 
Build a new image standing in the root-project-folder and execute `docker build -t microservice -f Docker/Dockerfile .` 
to create an image with name microservice.

To start the container `docker run -p8080:8080 microservice` to start a container that listens on port 8080 on
the local machine. Go to `http://localhost:8080/health` to check after the container has started. 

### IDEA
Start the main class by right click on it, should start without any special configuration.

## Build
Run `./gradlew assemble` and it will package the application into a runnable jar. Output is in build/libs/baseline.jar

## Deploy
Describe how this application is deployed in dev, test and production env.

## Release
When building for release (CircleCI), supply a parameter version on the commandline as so: `./gradlew assemble -Dversion=<releaseversion or hash>`

## Branching
Describe how the branching strategy is handled when developing this app.

## Tech

* JDK12
* [Micronaut](https://micronaut.io)
* [JUnit](https://junit.org/junit5/)
* [KotlinTest](https://github.com/kotlintest/kotlintest)

