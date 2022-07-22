rootProject.name = "trafficinfo-baseline-micronaut"

pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        id("org.jetbrains.kotlin.kapt") version kotlinVersion
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    }
}
include("baseline:cli", "baseline:domain", "baseline:client", "baseline:micronaut-reactor")