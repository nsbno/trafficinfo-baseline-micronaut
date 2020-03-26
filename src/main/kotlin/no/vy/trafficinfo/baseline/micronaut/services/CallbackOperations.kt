package no.vy.trafficinfo.baseline.micronaut.services

import io.micronaut.http.annotation.Body
import io.reactivex.Single

interface CallbackOperations {
    fun callback(@Body text: String): Single<String>
}