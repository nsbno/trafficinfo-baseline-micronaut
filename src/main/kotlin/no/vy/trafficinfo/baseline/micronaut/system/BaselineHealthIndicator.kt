package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import jakarta.inject.Singleton

@Singleton
class BaselineHealthIndicator : HealthIndicator {
    override fun getResult(): Publisher<HealthResult> {
        return Flux.just(FillrateHealthResult())
    }
}

class FillrateHealthResult : HealthResult {
    override fun getName(): String {
        return "trafficinfo-baseline"
    }

    override fun getDetails(): Any {
        return Health.InitHealth
    }

    override fun getStatus(): HealthStatus {
        return HealthStatus.UP
    }
}