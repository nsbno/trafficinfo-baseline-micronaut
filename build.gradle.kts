plugins {
    id ("org.jetbrains.kotlin.jvm") version "1.3.61"
    id ("org.jetbrains.kotlin.kapt") version "1.3.61"
    id ("org.jetbrains.kotlin.plugin.allopen")  version "1.3.61"
    id ("com.github.johnrengelman.shadow")  version "5.2.0"
    id ("application")
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

repositories {
    mavenCentral()
    jcenter()
    maven(
            url = "https://mvnrepo.cantara.no/content/repositories/releases"
    )
    maven(
            url = "https://repo.spring.io/libs-snapshot"
    )
}

val developmentOnly = configurations.create("developmentOnly")

configurations {
    developmentOnly
}

dependencies {
    implementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("javax.annotation:javax.annotation-api")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-discovery-client")
    implementation("io.micronaut.configuration:micronaut-aws-common")
    implementation("io.micronaut.configuration:micronaut-micrometer-registry-cloudwatch:1.3.0")
    implementation("io.micrometer:micrometer-registry-cloudwatch2:1.3.3")

    implementation("no.cantara.aws:sqs-util:0.7.3")
    implementation("com.amazonaws:aws-java-sdk-ssm:1.11.720")
    implementation("org.javers:javers-core:5.8.8")

    kapt("io.micronaut.configuration:micronaut-openapi")
    implementation("io.swagger.core.v3:swagger-annotations")

    kapt("io.micronaut:micronaut-security")
    implementation("io.micronaut:micronaut-security")
    implementation("io.micronaut:micronaut-security-jwt")

    kapt(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-inject-java")
    kapt("io.micronaut:micronaut-validation")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")

    kaptTest(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kaptTest("io.micronaut:micronaut-inject-java")

    testAnnotationProcessor("io.micronaut:micronaut-inject-java")

    testImplementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("io.mockk:mockk:1.9.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.assertj:assertj-core:3.15.0")
}

application {
    mainClassName = "no.vy.trafficinfo.baseline.micronaut.Application"
}

tasks {
    test {
        useJUnitPlatform ()
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
            attributes (mapOf(
                    "Implementation-Title" to rootProject.name,
                    "Implementation-Version" to artifactVersion
            ))
        }

    }

    (run) {
        doFirst {
            jvmArgs = listOf("-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote")

        }
    }
}
