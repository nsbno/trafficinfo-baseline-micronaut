package no.vy.trafficinfo.baseline.micronaut.jobs

import io.micronaut.scheduling.annotation.Scheduled
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEventRepository
import jakarta.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * # Scheduler to generate new events.
 */
@Singleton
class ChangeEventCreateJob(
    private val repo: ChangeEventRepository
) {

    /**
     * ## Create and emit a new change event with a random string.
     */
    @Scheduled(fixedDelay = "1s")
    fun createEvent() {
        repo.create()
    }
}