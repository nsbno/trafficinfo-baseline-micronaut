package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.reactivex.Single
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import no.vy.trafficinfo.baseline.micronaut.services.CallbackClient
import no.vy.trafficinfo.baseline.micronaut.services.WhoamiClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.annotation.security.RolesAllowed
import javax.inject.Inject

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