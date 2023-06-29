/**
 * Gradle build file.
 * Building the microservice with the Kotlin plugin for gradle.
 *
 * @see <a href="https://kotlinlang.org/docs/reference/using-gradle.html">Using Gradle in Official Kotlin doc.</a
 */
plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.allopen")
    id("io.micronaut.application") version "3.7.0"
    id("jacoco")
    id("org.sonarqube") version "3.3"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("io.micronaut.test-resources") version "3.7.8"
}

group = "no.vy.trafficinfo.baseline.micronaut"

val version: String by project

val micronautVersion: String by project
val targetJvmVersion: String by project

fun getProperty(name: String): String? {
    return if (project.properties[name] != null)
        project.properties[name].toString()
    else
        System.getenv(name)
}

repositories {
    maven {
        url = uri("https://nexus.common-services.vydev.io/repository/maven-public")
        credentials {
            username = getProperty("NEXUS_USERNAME")
            password = getProperty("NEXUS_PASSWORD")
        }
    }
}

micronaut {
    version(micronautVersion)
    runtime("netty")
    testRuntime("kotest")
    processing {
        incremental(true)
        annotations("no.vy.trafficinfo.*")
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

dependencies {
    /**
     * Kotlin dependencies.
     */
    implementation(kotlin("reflect"))

    /**
     * Micronaut framework dependencies.
     *
     * micronaut-inject-java and micronaut-validation are omitted
     * due to the micronaut application plugin adding them by default.
     */
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.openapi:micronaut-openapi")
    kapt("io.micronaut.security:micronaut-security")
    kapt("io.micronaut.micrometer:micronaut-micrometer-annotation")

    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")

    implementation("io.micronaut:micronaut-inject")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.discovery:micronaut-discovery-client")
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-cloudwatch")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut.reactor:micronaut-reactor-http-client")

    implementation("io.micronaut.security:micronaut-security")
    implementation("io.micronaut.security:micronaut-security-jwt")

    // AWS service dependencies
    implementation("io.micronaut.aws:micronaut-aws-sdk-v2")
    implementation("io.micronaut.aws:micronaut-aws-parameter-store")
    implementation("software.amazon.awssdk:dynamodb")

    implementation("io.micronaut.cache:micronaut-cache-caffeine")
    implementation("io.micronaut.problem:micronaut-problem-json")

    /**
     * Trafficinfo Common Dependencies.
     */
    implementation("no.vy.trafficinfo.common:logging:0.0.3")

    /**
     * Third-party dependencies.
     */
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names")
    implementation("com.fasterxml.jackson.module:jackson-module-blackbird")

    /**
     * kotlin coroutines
     */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    /**
     * Micronaut supports context propagation from Reactor’s context to coroutine context.
     * To enable this propagation you need to include following dependency
     */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
    /**
     * Tracing
     */
    implementation("io.micronaut:micronaut-tracing")
    implementation("co.elastic.apm:apm-agent-api:1.33.0")
    implementation("co.elastic.apm:apm-opentracing:1.33.0")

    /**
     * Test dependency configurations.
     */
    testCompileOnly(platform("io.micronaut:micronaut-bom:$micronautVersion"))

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.33.2")
    testImplementation("io.mockk:mockk")
    testImplementation("io.micronaut:micronaut-inject-java")
    testImplementation("org.assertj:assertj-core")
    testImplementation("io.micronaut.test:micronaut-test-kotest5")
    testImplementation("io.kotest:kotest-runner-junit5-jvm")
}

application {
    mainClass.set("no.vy.trafficinfo.baseline.micronaut.Application")
}

jacoco {
    toolVersion = "0.8.8"
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
        baseImage("gcr.io/distroless/cc-debian11")
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

    compileKotlin {
        kotlinOptions {
            jvmTarget = targetJvmVersion
            javaParameters = true
        }
    }

    compileTestKotlin {
        kotlinOptions {
            jvmTarget = targetJvmVersion
            javaParameters = true
        }
    }

    runnerJar {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Title" to rootProject.name,
                    "Implementation-Version" to project.version
                )
            )
        }
    }

    (run) {
        doFirst {
            jvmArgs = listOf("-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote")
        }
    }
}