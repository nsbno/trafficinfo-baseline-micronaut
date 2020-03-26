package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.*
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/whoami")
class WhoamiController {
    private val log: Logger = LoggerFactory.getLogger(WhoamiController::class.java)

    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami(
        authentication: Authentication?
    ): HttpResponse<String> {
        log.info("Open Whoami called.")
        return HttpResponse.ok("Open Whoami called")
    }
}