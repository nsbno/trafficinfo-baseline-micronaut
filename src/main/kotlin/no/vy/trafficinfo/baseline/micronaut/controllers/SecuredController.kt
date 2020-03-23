package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import java.time.LocalDate
import javax.inject.Inject
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import no.vy.trafficinfo.common.client.trainroute.TrainRouteClient
import no.vy.trafficinfo.domain.trainroute.CountryCode
import no.vy.trafficinfo.domain.trainroute.Train
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/secured")
class SecuredController {
    private val log: Logger = LoggerFactory.getLogger(SecuredController::class.java)

    @Inject
    lateinit var trainRouteClient: TrainRouteClient

    /**
     * Test Generic secured endpoint.
     * Just return some basic Health info.
     */
    @Get()
    @Produces(MediaType.APPLICATION_JSON)
    fun securedHealth(): HttpResponse<Health> {
        log.info("Secured health called")
        return HttpResponse.ok(Health())
    }

    /**
     * To test A2A communication to trainroute with authentication.
     * Should propagate the incoming access token by default.
     */
    @Get("/trainroute")
    @Produces(MediaType.APPLICATION_JSON)
    fun securedTrainroute(): HttpResponse<Health> {
        log.info("Secured Trainroute")
        trainRouteClient.getTrain(CountryCode.NO, "512", LocalDate.now())
                .subscribe(
                        { x: Train -> print("Emitted item: $x") },
                        { ex: Throwable -> println("Error: " + ex.message) },
                        { println("Completed. No items.") }
                )
        return HttpResponse.ok(Health())
    }
}