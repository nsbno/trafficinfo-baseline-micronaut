package no.vy.trafficinfo.baseline.micronaut.services

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import reactor.core.publisher.Mono

// @AccessTokenAuth TODO Change this to a logging-filter
@Client("baseline-micronaut")
interface CallbackClient : CallbackOperations {

    @Post("/secured/callback")
    override fun callback(@Body text: String): Mono<String>
}