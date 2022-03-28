package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.context.annotation.Property
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.management.health.indicator.annotation.Liveness
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import jakarta.inject.Singleton

@Liveness
@Singleton
class UptimeHealthIndicator(@Property(name = "micronaut.application.name") val applicationName: String) : HealthIndicator {
    override fun getResult(): Publisher<HealthResult> {
        return Mono.just(
            HealthResult.builder(applicationName)
                .details(Health())
                .status(HealthStatus.UP)
                .build()
        )
    }
}