package no.vy.trafficinfo.baseline.micronaut.jobs

import io.micronaut.core.annotation.Introspected
import io.micronaut.scheduling.annotation.Scheduled
import io.opentelemetry.extension.annotations.WithSpan
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEventRepository
import jakarta.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * # Scheduler to generate new events.
 */
@Singleton
@Introspected
open class ChangeEventCreateJob(
    private val repo: ChangeEventRepository
) {

    /**
     * ## Create a new change event in the repository.
     *
     * As a side effect the repository will broadcast
     * the newly created ChangeEvent on the internal
     * micronaut EventPublisher bus which the
     * ChangeController listens for.
     */
    @Scheduled(fixedDelay = "1s")
    @WithSpan
    open fun createEvent() {
        logger.info { "Scheduler triggered create new ChangeEvent." }
        repo.create()
    }
}