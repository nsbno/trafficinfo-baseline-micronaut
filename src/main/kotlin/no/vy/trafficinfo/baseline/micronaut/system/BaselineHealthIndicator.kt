package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import io.reactivex.Flowable
import javax.inject.Singleton
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import org.reactivestreams.Publisher

@Singleton
class BaselineHealthIndicator : HealthIndicator {
    override fun getResult(): Publisher<HealthResult> {
        return Flowable.just(FillrateHealthResult())
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