# Development

## Pre-requisites
Description of everything needed to be installed to start developing the application.

## Run
### maven
Run `mvn exec:exec` and it will start the application.

### docker 
Build a new image standing in the root-project-folder and execute `docker build -t microservice -f Docker/Dockerfile .` 
to create an image with name microservice.

To start the container `docker run -p8080:8080 microservice` to start a container that listens on port 8080 on
the local machine. Go to `http://localhost:8080/health` to check after the container has started. 

### IDEA
Start the main class by right click on it, should start without any special configuration.

## Build
Describe how to build the application.

## Deploy
Describe how this application is deployed in dev, test and production env.

## Release
Describe how a new release is performed.

## Branching
Describe how the branching strategy is handled when developing this app.

## Tech

* JDK12
* [Jetty Web Server](https://www.eclipse.org/jetty/)
* [Jersey JAX-RS](https://jersey.github.io/)
* [TestNG](https://github.com/cbeust/testng)
* [REST-assured: Java DSL for easy testing of REST services](https://github.com/rest-assured/rest-assured)
