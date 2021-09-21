package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A resource that will throw a RuntimeException and log an ERROR when called.
 */
@Controller("/crash")
class CrashController {

    private val log: Logger = LoggerFactory.getLogger(CrashController::class.java)

    /**
     * Crash endpoint.
     * Use to trigger to many errors alarm to verify Statuspage and Pagerduty integration.
     */
    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    fun crash(): HttpResponse<String> {
        return HttpResponse.serverError("Crash was called.").also {
            log.error("Crash was called.")
        }
    }
}