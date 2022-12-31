package no.vy.trafficinfo.baseline.micronaut.system.clients

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client


/**
 * # Declarative Http Client for the Whoami Service.
 */
@Client("whoami")
interface WhoamiClient {
    @Get(value = "/")
    fun get(): HttpResponse<String>
}
