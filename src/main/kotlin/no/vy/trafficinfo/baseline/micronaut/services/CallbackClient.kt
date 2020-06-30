package no.vy.trafficinfo.baseline.micronaut.services

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.reactivex.Single
// import no.vy.trafficinfo.common.security.client.filters.AccessTokenAuth

// @AccessTokenAuth
@Client("baseline-micronaut")
interface CallbackClient : CallbackOperations {

    @Post("/secured/callback")
    override fun callback(@Body text: String): Single<String>
}