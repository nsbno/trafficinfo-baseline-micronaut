package no.vy.trafficinfo.baseline.micronaut.domain

/**
 * Is a data class that just contains a random string.
 * Used by ChangeController as a simple return type.
 */
data class ChangeEvent(
    val payload: String,
    val version: Long
)