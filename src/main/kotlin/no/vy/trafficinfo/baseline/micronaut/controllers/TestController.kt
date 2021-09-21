package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A secured resource that you need an access token to call.
 *
 * This endpoint is just an example of how to do authorization and authentication
 * and in addition also fine grained access control using custom scopes from
 * Cognito.
 *
 * To call any of these resources you need to set a Authorization: Bearer <token>
 * http header on the request. The token is retrieved from Cognito like described
 * in confluence https://jico.nsb.no/confluence/display/TRAFFICINFO/Authentication+and+Authorization
 */
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/test")
class TestController {

    private val log: Logger = LoggerFactory.getLogger(TestController::class.java)

    /**
     * Secured health endpoint.
     * Everyone that is authenticated should be able to call this endpoint.
     */
    @Get("/ok")
    @Produces(MediaType.APPLICATION_JSON)
    fun health(): HttpResponse<Health> {
        return HttpResponse.ok(Health())
    }

    @Get("/error")
    @Produces(MediaType.APPLICATION_JSON)
    fun error(): HttpResponse<String> {
        return HttpResponse.serverError("Error was called.").also {
            log.error("Error was called.")
        }
    }
}