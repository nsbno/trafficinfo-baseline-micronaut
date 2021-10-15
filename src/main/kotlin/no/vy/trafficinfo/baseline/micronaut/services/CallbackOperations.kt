package no.vy.trafficinfo.baseline.micronaut.services

import io.micronaut.http.annotation.Body
import reactor.core.publisher.Mono

interface CallbackOperations {
    fun callback(@Body text: String): Mono<String>
}