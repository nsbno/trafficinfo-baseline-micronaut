/**
 * Gradle build file.
 * Building the microservice with the Kotlin plugin for gradle.
 *
 * @see <a href="https://kotlinlang.org/docs/reference/using-gradle.html">Using Gradle in Official Kotlin doc.</a
 */

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("io.micronaut.application") version "3.1.1"
    id("org.jetbrains.kotlin.plugin.allopen")
    id("jacoco")
    id("org.sonarqube") version "3.3"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

group = "no.vy.trafficinfo.baseline.micronaut"

val version: String by project
val micronautVersion: String by project
val spekVersion: String by project
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

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("no.vy.trafficinfo.baseline.micronaut.*")
    }
}

kotlin {
    // Opens up the the required compiler packages to ensure KAPT works with JDK16
    kotlinDaemonJvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
    )
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.GRAAL_VM)
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
    kapt(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.openapi:micronaut-openapi")
    kapt("io.micronaut.security:micronaut-security")
//    kapt("io.micronaut.micrometer:micronaut-micrometer-annotation")

    implementation(enforcedPlatform("io.micronaut:micronaut-bom:$micronautVersion"))
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")

    implementation("io.micronaut:micronaut-inject")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-tracing")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.discovery:micronaut-discovery-client")
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-cloudwatch")
    implementation("io.micronaut.reactor:micronaut-reactor")

    implementation("io.micronaut.security:micronaut-security")
    implementation("io.micronaut.security:micronaut-security-jwt")

    // implementation("io.micronaut.aws:micronaut-aws-sdk-v1") // Switch to this to use sdk-v1 from aws, Required by cantara sqs-util
    implementation("io.micronaut.aws:micronaut-aws-sdk-v2")
    implementation("io.micronaut.aws:micronaut-aws-parameter-store")

    implementation("io.micronaut.cache:micronaut-cache-caffeine")
    implementation("io.micronaut.problem:micronaut-problem-json")

    /**
     * Trafficinfo Common Dependencies.
     */
    implementation("no.vy.trafficinfo.common:logging:0.0.3")

    /**
     * Third-party dependencies.
     */
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names")
    implementation("com.fasterxml.jackson.module:jackson-module-blackbird")

    /**
     * Test dependency configurations.
     */
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.assertj:assertj-core")
    testImplementation("com.github.tomakehurst:wiremock:2.27.2")
    testImplementation("io.mockk:mockk:1.12.1")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

application {
    mainClass.set("no.vy.trafficinfo.baseline.micronaut.Application")
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    // Optionally configure plugin
    ktlint {
        debug.set(true)
    }
}

jacoco {
    toolVersion = "0.8.7"
}

tasks {
    jacocoTestReport {
        reports {
            xml.required.set(true)
            html.required.set(false)
            csv.required.set(false)
        }
    }

    test {
        systemProperty("micronaut.environments", "test")
        systemProperty("micronaut.env.deduction", false)
        dependsOn(ktlintCheck)
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

    (run) {
        doFirst {
            jvmArgs = listOf("-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote")
        }
    }
}