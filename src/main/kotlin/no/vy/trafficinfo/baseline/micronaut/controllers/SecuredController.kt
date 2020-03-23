package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.reactivex.Single
import javax.inject.Inject
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import no.vy.trafficinfo.baseline.micronaut.services.WhoamiClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/secured")
class SecuredController {
    private val log: Logger = LoggerFactory.getLogger(SecuredController::class.java)

    @Inject
    lateinit var whoamiClient: WhoamiClient

    /**
     * Test Generic secured endpoint.
     * Just return some basic Health info.
     */
    @Get("/health")
    @Produces(MediaType.APPLICATION_JSON)
    fun securedHealth(): HttpResponse<Health> {
        log.info("Secured health called")
        return HttpResponse.ok(Health())
    }

    /**
     * To test A2A communication to Whoami with authentication.
     * Should propagate the incoming access token by default.
     */
    @Get("/whoami")
    @Produces(MediaType.APPLICATION_JSON)
    fun securedWhoami(): Single<String> {
        log.info("Secured Whoami")
        return whoamiClient.whoami()
    }
}