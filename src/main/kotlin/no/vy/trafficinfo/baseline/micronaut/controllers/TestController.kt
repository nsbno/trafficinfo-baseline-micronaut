package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 */
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/test")
class TestController {

    private val log: Logger = LoggerFactory.getLogger(TestController::class.java)

    /**
     * Failing endpoint
     */
    @Get("/error")
    fun crash(): HttpResponse<String> {
        return HttpResponse.serverError("This endpoint always fails.")
    }
}