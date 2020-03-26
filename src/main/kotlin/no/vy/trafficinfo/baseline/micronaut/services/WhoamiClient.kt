package no.vy.trafficinfo.baseline.micronaut.services

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.reactivex.Single
import no.vy.trafficinfo.common.security.client.filters.AccessTokenAuth

@AccessTokenAuth
@Client("whoami")
interface WhoamiClient : WhoamiOperations {

    @Get("/whoami")
    override fun whoami(): Single<String>
}