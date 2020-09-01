/**
 * Gradle build file.
 * Building the microservice with the Kotlin plugin for gradle.
 *
 * @see <a href="https://kotlinlang.org/docs/reference/using-gradle.html">Using Gradle in Official Kotlin doc.</a
 */

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.allopen")

    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("application")
    id("org.jlleitschuh.gradle.ktlint") version "9.3.0"
    id("org.sonarqube") version "3.0"
    id("jacoco")
}

group = "no.vy.trafficinfo.baseline.micronaut"

val version: String by project
val micronautVersion: String by project
val kotlinVersion: String by project
val spekVersion: String by project
val junitVersion: String by project
val artifactGroup = group
val artifactVersion = version
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

val developmentOnly = configurations.create("developmentOnly")

configurations {
    developmentOnly
}

dependencies {
    /**
     * Kotlin dependencies.
     */
    implementation(kotlin("reflect"))

    /**
     * Micronaut framework dependencies.
     */
    annotationProcessor(enforcedPlatform("io.micronaut:micronaut-bom:$micronautVersion"))
    annotationProcessor("io.micronaut:micronaut-inject-java")
    annotationProcessor("io.micronaut:micronaut-validation")
    annotationProcessor("io.micronaut.configuration:micronaut-openapi")
    annotationProcessor("io.micronaut.security:micronaut-security")

    implementation(enforcedPlatform("io.micronaut:micronaut-bom:$micronautVersion"))
    implementation("io.micronaut:micronaut-inject")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-discovery-client")
    implementation("io.micronaut.aws:micronaut-aws-common")
    implementation("io.micronaut.configuration:micronaut-micrometer-registry-cloudwatch:1.3.1")
    implementation("io.micronaut:micronaut-tracing")
    implementation("io.micronaut.security:micronaut-security")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.aws:micronaut-aws-parameter-store:$micronautVersion")
    implementation("io.micronaut.cache:micronaut-cache-caffeine")

    kapt(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-inject-java")
    kapt("io.micronaut:micronaut-validation")
    kapt("io.micronaut.security:micronaut-security")
    kapt("io.micronaut.configuration:micronaut-openapi")

    /**
     * Trafficinfo Common Dependencies.
     */
    implementation("no.vy.trafficinfo.common:logging:0.0.2")
    implementation("no.vy.trafficinfo.common:security:0.1.0")

    /**
     * Third-party dependencies.
     */
    implementation("com.amazonaws:aws-java-sdk-ssm:1.11.814")
    implementation("no.cantara.aws:sqs-util:0.7.6")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    /**
     * Test dependency configurations.
     */
    testAnnotationProcessor("io.micronaut:micronaut-inject-java")
    testImplementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.assertj:assertj-core:3.17.1")
    testImplementation("com.github.tomakehurst:wiremock:2.27.1")
    testImplementation("io.mockk:mockk:1.10.0")

    kaptTest(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kaptTest("io.micronaut:micronaut-inject-java")
    kaptTest("io.micronaut:micronaut-validation")
}

application {
    mainClassName = "no.vy.trafficinfo.baseline.micronaut.Application"
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    // Optionally configure plugin
    ktlint {
        debug.set(true)
    }
}

tasks {
    jacocoTestReport {
        reports {
            xml.isEnabled = true
            html.isEnabled = false
            csv.isEnabled = false
        }
    }

    test {
        useJUnitPlatform()
        classpath += developmentOnly
        systemProperty("micronaut.environments", "test")
        systemProperty("micronaut.env.deduction", false)
    }

    allOpen {
        annotation("io.micronaut.aop.Around")
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

    shadowJar {
        mergeServiceFiles()
        archiveFileName.set("baseline.jar")
        manifest {
            attributes(
                mapOf(
                    "Implementation-Title" to rootProject.name,
                    "Implementation-Version" to artifactVersion
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