package no.vy.trafficinfo.baseline.micronaut.jobs

import io.micronaut.core.annotation.Introspected
import io.micronaut.scheduling.annotation.Scheduled
import co.elastic.apm.api.Traced
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.services.CreateEventService
import jakarta.inject.Named
import jakarta.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * # Scheduler to generate new events.
 */
@Singleton
@Introspected
open class ChangeEventCreateJob(
    @Named("create_event")
    private val createEventService: CreateEventService
) : ChangeEventCreateJobInterface {
    @Scheduled(fixedDelay = "10s")
    @Traced
    override fun createEvent() {
        logger.info { "Scheduler triggered create new ChangeEvent." }
        createEventService.createEvent()
    }
}

interface ChangeEventCreateJobInterface {
    fun createEvent()
}