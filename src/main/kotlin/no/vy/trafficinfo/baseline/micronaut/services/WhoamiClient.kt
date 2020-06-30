package no.vy.trafficinfo.baseline.micronaut.services

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.reactivex.Single
// import no.vy.trafficinfo.common.security.client.filters.AccessTokenAuth

/**
 * Client to call WHOAMI service for testing.
 * The AccessTokenAuth annotation used is an annotation from common-security
 * that is used to match clients to the HttpClientFilter that checks if a
 * token has been retrieved from Cognito, and if the token is missing or has
 * expired retrieves a new one from Cognito.
 */
// @AccessTokenAuth
@Client("whoami")
interface WhoamiClient : WhoamiOperations {

    @Get("/whoami")
    override fun whoami(): Single<String>
}