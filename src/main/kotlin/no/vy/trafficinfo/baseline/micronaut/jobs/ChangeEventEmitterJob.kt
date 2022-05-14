package no.vy.trafficinfo.baseline.micronaut.jobs

import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.scheduling.annotation.Scheduled
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEvent
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEventRepository
import jakarta.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * # Scheduler to generate new events.
 */
@Singleton
class ChangeEventEmitterJob(
    private val repo: ChangeEventRepository,
    private val eventPublisher: ApplicationEventPublisher<ChangeEvent>
) {

    /**
     * ## Create and emit a new change event with a random string.
     */
    @Scheduled(fixedDelay = "10s")
    fun emitEvents() {
        logger.info { "Publish new ChangeEvent" }
        eventPublisher.publishEventAsync(repo.create())
    }
}