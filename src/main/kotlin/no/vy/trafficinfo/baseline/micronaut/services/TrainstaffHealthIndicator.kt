package no.vy.trafficinfo.baseline.micronaut.services

import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import io.reactivex.Flowable
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import org.reactivestreams.Publisher
import javax.inject.Singleton

@Singleton
class TrainstaffHealthIndicator : HealthIndicator {
    override fun getResult(): Publisher<HealthResult> {
        return Flowable.just(FillrateHealthResult())
    }
}

class FillrateHealthResult : HealthResult {
    override fun getName(): String {
        return "trafficinfo-trainstaff"
    }

    override fun getDetails(): Any {
        return Health.InitHealth
    }

    override fun getStatus(): HealthStatus {
        return HealthStatus.UP
    }

}