rootProject.name = "trafficinfo-baseline-micronaut"

pluginManagement {
    val kotlinVersion = providers.gradleProperty("kotlinVersion").get()
    val kspVersion = providers.gradleProperty("kspVersion").get()
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("com.google.devtools.ksp") version kspVersion
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}