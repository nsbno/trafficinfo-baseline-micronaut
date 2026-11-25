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
- JDK 25 (compile and runtime). `.sdkmanrc` pins it for SDKMAN (`sdk env`). Gradle uses a JDK 25
  toolchain and can download it through the foojay resolver if it isn't installed.
- Docker, for the tests (Micronaut test-resources starts LocalStack) and for building the image.
- `NEXUS_USERNAME` / `NEXUS_PASSWORD` as environment variables or Gradle properties, for the
  internal Nexus repository.

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
Run `./gradlew build` to compile, run ktlint and the tests, and prepare the Docker layers in
`build/docker/main/layers/`, which `Docker/Dockerfile` copies:

| Layer folder     | Contents                                         | Copied to (in the image)  |
|------------------|--------------------------------------------------|---------------------------|
| `libs/`          | release dependencies                             | `application/libs/`       |
| `project_libs/`  | dependencies on other Gradle projects in the build | `application/libs/`     |
| `snapshot_libs/` | `-SNAPSHOT` dependencies                         | `application/libs/`       |
| `resources/`     | `src/main/resources`                             | `application/resources/`  |
| `app/`           | `application.jar`                                | `application/`            |

Caveats:
- **`project_libs/` and `snapshot_libs/` must be merged into `libs/`.** The `Class-Path` in
  `application.jar`'s manifest lists every dependency as `libs/<jar>`, including `-SNAPSHOT` ones,
  even though the plugin puts them in separate folders. If they are copied anywhere else (or not
  at all), the image builds fine but the app fails at runtime with `ClassNotFoundException`, or
  loses resources such as the logback config from `no.vy.trafficinfo.common:logging`. This is the
  same layout as the plugin's own generated Dockerfile (`./gradlew dockerfile`).
- The plugin always creates all layer folders, even empty ones, so the Dockerfile copies them
  unconditionally. If a plugin upgrade changes that (the `COPY` fails on a missing folder) or
  changes the layout, compare with `build/docker/main/Dockerfile` from `./gradlew dockerfile`.
- Micronaut Gradle plugin 5 moved `application.jar` into `app/`. Before that it was at the root of
  `layers/`.
- To check an image, make sure every manifest entry exists (only `classes/` may be missing):
  ```bash
  docker run --rm --entrypoint sh <image> -c 'cd application && for f in $(unzip -p application.jar META-INF/MANIFEST.MF \
    | sed -e ":a;N;\$!ba;s/\r\?\n //g" | grep -i class-path | tr -d "\r" | cut -d" " -f2-); do [ -e "$f" ] || echo "MISSING $f"; done'
  ```

## Testing
Tests use Kotest 6 (managed by the Micronaut platform) with `micronaut-test-kotest5`, plus
Micronaut test-resources, which starts a LocalStack container for SNS, SQS and DynamoDB.

Caveats:
- **Kotest project config must be `io.kotest.provided.ProjectConfig`.** Kotest 6 no longer scans
  the classpath for it, so a config in another package is silently ignored and the Micronaut
  extension isn't registered.
- **Kotest 6 creates every spec, and so every `@MicronautTest` application context, before any
  test runs**, and the contexts stay open at the same time. Beans that consume shared external
  resources (such as the JMS listener on the LocalStack test queue) then exist once per spec and
  compete with each other. Limit such beans to the spec that needs them with an environment, as
  `TextMessageConsumer` does (`@Requires(env = …)` plus `@MicronautTest(environments = […])`).
  `--tests` filters don't change this: the other specs are still created.
- **Declare the scope of `@JMSListener` beans** (`@Singleton`) if a test needs the same instance
  that receives the messages.
- **LocalStack image: use `localstack/localstack:community-archive`.** Newer LocalStack images
  require an auth token. Versions older than 3.x don't support the AWS JSON protocol that the
  current AWS SDK v2 uses for SQS (`createQueue` returns a `null` queue URL).
- `until` was removed in Kotest 6; use `eventually` from `io.kotest.assertions.nondeterministic`.

## Upgrade Micronaut to new versions
See official docs of breaking changes
https://docs.micronaut.io/latest/guide/index.html#breaks

### Micronaut 1.3 to 2.0
Quite a lot of changes to dependencies and also possible code changes needed when upgrading.
It usually works without serious trouble if all dependencies has been updated to new groups.
https://docs.micronaut.io/2.0.0/guide/index.html#upgrading

### Micronaut 2.0 to 2.1 
Simple upgrade with just minor changes needed, https://docs.micronaut.io/2.1.0/guide/index.html#upgrading

### Micronaut 2.1 to 2.2
Also very simple upgrade, https://docs.micronaut.io/2.2.0/guide/index.html#upgrading

### Micronaut 4 to 5
Upgrade guide: https://github.com/micronaut-projects/micronaut-core/wiki/Update-to-Micronaut-5.
For module details use each module's **versioned** guide; the `latest` and snapshot guides can lag
behind. Several changes fail **silently**, so a green build isn't enough:

- **Toolchain:** JDK 25, Kotlin ≥ 2.3, KSP2 (kapt is gone), Gradle ≥ 9.5, Micronaut Gradle
  plugin 5.x. Update the Docker base image, `.sdkmanrc` and CircleCI (see [Deploy](#deploy)) too.
- **Annotation processors** go on `ksp(...)`. `annotationProcessor(...)` does nothing for Kotlin
  sources. The security processor is now `micronaut-security-processor`.
- **Jackson 3:** new packages and renamed `jackson.*` keys (old keys are ignored). See
  [Configuration](configuration.md#jackson-json).
- **Bootstrap / config client removed:** Parameter Store is loaded with `micronaut.config.import`,
  which changes which paths are read and which value wins on duplicate keys. See
  [Configuration](configuration.md#aws-parameter-store).
- **JSpecify nullability:** Micronaut 5 APIs are `@NullMarked`, so Kotlin may require non-null
  type arguments (e.g. `ServerRequestContext.currentRequest<Any>()` instead of `<Any?>`) and
  null checks on values like `PropertySource.get(key)`.
- **Kotest 6, LocalStack and the build layout** changed as well; see [Testing](#testing) and
  [Build](#build).
- **Internal libraries** that contain Micronaut-generated classes must be built for Micronaut 5.
  `no.vy.trafficinfo.common:logging` only contains a logback XML and isn't affected.
- **ktlint 1.8** rejects KDoc (`/** … */`) inside blocks and dangling KDoc. Use `/* … */` there.

## Known build warnings
These warnings are expected with the current dependency versions. They are deliberately **not**
silenced with JVM flags, so they keep reminding us until the dependency is fixed. Recheck them
when upgrading. Use `./gradlew build --warning-mode all` to see Gradle deprecations with their
source.

| Warning | Source | Fixed by |
|---|---|---|
| `PropertiesFileContentMerger.withProperties` and `Using a Project object as a dependency notation` (Gradle 10 deprecations) | Micronaut Gradle plugin (applies the Eclipse APT and GraalVM native plugins) | A Micronaut Gradle plugin release that supports Gradle 10 |
| `sun.misc.Unsafe::objectFieldOffset … org.jetbrains.kotlin.com.intellij.util.containers.Unsafe` | ktlint workers (`kotlin-compiler-embeddable` bundled with ktlint) | A newer ktlint release |
| `System::loadLibrary has been called by io.netty.util.internal.NativeLibraryUtil` | Netty in the Micronaut test-resources service JVM (started by the Gradle plugin) | A Netty/test-resources release that declares native access |
| `[ksp] File dummy is generated from a non-aggregating visitor…` | Micronaut KSP processor | Harmless; upstream issue |

Watch for JDK changes as well: if a future JDK makes `sun.misc.Unsafe` memory access or restricted
native methods deny-by-default, these warnings become errors.

## Code Linting
Code linting is done with ktlint and a gradle plugin running the linting when building the code.
There should be a commit hook checking the code on pre-commit so that you cant commit code that 
does not satisfy linting rules. When detecting lint errors, auto-format the code to match ktlint 
rules with `gradle ktlintFormat` command and all the code in the repo will be auto-formatted. 

## Deploy
Dev is a rapid development environment that has no automated pipelines, it is maintained
with `terraform apply` by the developers.

### CircleCI build images
Both CircleCI jobs run on machine executors (the tests need Docker). Use `ubuntu-2404:current`,
not a dated tag: CircleCI removes old dated images, which makes pipelines fail. That happened with
`ubuntu-2204:2023.04.2`. Orb executors can also pin removed images: `aws-ecr@8.2.1` defaults to
`ubuntu-2004:202107-02`, so `build-and-push-image` overrides the image explicitly. Check this
when you upgrade an orb.

The build job installs JDK 25 itself (a `pre-steps` step) instead of relying on the Java version
that ships with the machine image.

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