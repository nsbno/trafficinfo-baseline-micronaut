package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import no.vy.trafficinfo.baseline.micronaut.domain.Health

@Controller("/health")
class HealthController {

    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    fun health(): HttpResponse<Health> {
        return HttpResponse.ok(Health())
    }
}