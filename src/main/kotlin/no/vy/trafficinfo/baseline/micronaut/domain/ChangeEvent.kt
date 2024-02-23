package no.vy.trafficinfo.baseline.micronaut.domain

import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Is a data class that just contains a random string.
 * Used by ChangeController as a simple return type.
 */
@Serdeable
data class ChangeEvent(
    @field:Schema(description = "The payload", example = "V0HK5GgigW")
    val payload: String,

    @field:Schema(description = "The version number", example = "12932")
    val version: Long,
)