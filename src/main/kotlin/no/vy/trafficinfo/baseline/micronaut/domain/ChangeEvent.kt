package no.vy.trafficinfo.baseline.micronaut.domain

/**
 * Return type from Controller.
 */
data class ChangeEvent(
    val payload: String,
    val version: Long
)