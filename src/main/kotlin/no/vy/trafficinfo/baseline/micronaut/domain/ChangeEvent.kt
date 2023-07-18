package no.vy.trafficinfo.baseline.micronaut.domain

import io.micronaut.serde.annotation.Serdeable

/**
 * Is a data class that just contains a random string.
 * Used by ChangeController as a simple return type.
 */
@Serdeable
data class ChangeEvent(
    val payload: String,
    val version: Long
)