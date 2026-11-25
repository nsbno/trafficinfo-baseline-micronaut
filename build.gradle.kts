/*
 * Gradle build file.
 * Building the microservice with the Kotlin plugin for gradle.
 *
 * @see <a href="https://kotlinlang.org/docs/reference/using-gradle.html">Using Gradle in Official Kotlin doc.</a
 */
plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("com.google.devtools.ksp")

    id("io.micronaut.application") version "5.0.2"
    id("io.micronaut.test-resources") version "5.0.2"

    id("jacoco")
    id("org.sonarqube") version "7.6.0.18749"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

group = "no.vy.trafficinfo.baseline.micronaut"

val micronautVersion = project.property("micronautVersion").toString()

fun getProperty(name: String): String? = project.findProperty(name)?.toString() ?: System.getenv(name)

repositories {
    maven {
        url = uri("https://nexus.common-services.vydev.io/repository/maven-public")
        credentials {
            username = getProperty("NEXUS_USERNAME")
            password = getProperty("NEXUS_PASSWORD")
        }
    }
    mavenCentral()
}

micronaut {
    version(micronautVersion)
    runtime("netty")
    testRuntime("kotest5")
    processing {
        incremental(true)
        annotations("no.vy.trafficinfo.*")
    }
}

ktlint {
    // the plugin default bundles an old Kotlin compiler
    version.set("1.8.0")
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    /*
     * Kotlin dependencies.
     */
    implementation(kotlin("reflect"))

    /*
     * Micronaut framework dependencies.
     *
     * micronaut-inject-kotlin is omitted
     * due to the micronaut application plugin adding it by default.
     */
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.openapi:micronaut-openapi")
    ksp("io.micronaut.security:micronaut-security-processor")
    ksp("io.micronaut.micrometer:micronaut-micrometer-annotation")
    ksp("io.micronaut.validation:micronaut-validation-processor")

    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")

    implementation("io.micronaut:micronaut-inject")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.discovery:micronaut-discovery-client")
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-cloudwatch")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut.reactor:micronaut-reactor-http-client")

    implementation("io.micronaut.security:micronaut-security")
    implementation("io.micronaut.security:micronaut-security-jwt")

    implementation("io.micronaut.jms:micronaut-jms-sqs")

    // AWS service dependencies
    implementation("io.micronaut.aws:micronaut-aws-sdk-v2")
    implementation("io.micronaut.aws:micronaut-aws-parameter-store")
    implementation("software.amazon.awssdk:dynamodb")
    implementation("software.amazon.awssdk:sqs")
    implementation("software.amazon.awssdk:sns")

    implementation("io.micronaut.cache:micronaut-cache-caffeine")
    implementation("io.micronaut.problem:micronaut-problem-json")

    /*
     * Trafficinfo Common Dependencies.
     */
    implementation("no.vy.trafficinfo.common:logging:0.0.6")

    /*
     * Third-party dependencies.
     */
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("io.swagger.core.v3:swagger-annotations")

    /*
     * kotlin coroutines
     */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    implementation("org.yaml:snakeyaml")

    /*
     * Micronaut supports context propagation from Reactor’s context to coroutine context.
     * To enable this propagation you need to include following dependency
     */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    /*
     * Tracing
     */
    implementation("co.elastic.apm:apm-agent-api:1.57.1")
    implementation("co.elastic.apm:apm-opentracing:1.57.1")

    /*
     * Test dependency configurations.
     */
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("io.mockk:mockk")
    testImplementation("org.assertj:assertj-core")
    testImplementation("io.micronaut.test:micronaut-test-kotest5")
    testImplementation("io.kotest:kotest-runner-junit5-jvm")
}

application {
    mainClass.set("no.vy.trafficinfo.baseline.micronaut.Application")
}

jacoco {
    toolVersion = "0.8.16"
}

tasks {

    // configure graalvm native-image to include reflection classes.
    // classes not auto-discovered needs to be added manually as either Proxy, Resource or reflect config json files.
    graalvmNative {
        binaries {
            named("main") {
//                verbose.set(true) // To see what configurations are auto-discovere by native-image when starting build.
                buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
                buildArgs.add("-H:ClassInitialization=org.slf4j:build_time")
            }
        }
    }

    // use Google Distroless mostly-static image when generating the native-image build Dockerfile.
    dockerfileNative {
        baseImage("gcr.io/distroless/cc-debian12")
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
            html.required.set(false)
            csv.required.set(false)
        }
    }

    test {
        useJUnitPlatform()
        systemProperty("micronaut.environments", "test")
        systemProperty("micronaut.env.deduction", false)
    }

    runnerJar {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Title" to rootProject.name,
                    "Implementation-Version" to project.version,
                ),
            )
        }
    }
}