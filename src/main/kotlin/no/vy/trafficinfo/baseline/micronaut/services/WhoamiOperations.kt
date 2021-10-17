package no.vy.trafficinfo.baseline.micronaut.services

import reactor.core.publisher.Mono

interface WhoamiOperations {
    fun whoami(): Mono<String>
}