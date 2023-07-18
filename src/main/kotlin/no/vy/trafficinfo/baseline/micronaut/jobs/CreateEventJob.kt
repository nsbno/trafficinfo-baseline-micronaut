package no.vy.trafficinfo.baseline.micronaut.jobs

import io.micronaut.core.annotation.Introspected
import co.elastic.apm.api.Traced
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.services.CreateEventService
import no.vy.trafficinfo.baseline.micronaut.services.RandomStringServiceException
import kotlin.concurrent.thread
import jakarta.annotation.PostConstruct
import jakarta.inject.Named
import jakarta.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * # Scheduler to generate new events.
 */
@Singleton
@Introspected
open class EventCreateJob(
    @Named("create_event")
    private val createEventService: CreateEventService
) : EventCreateJobInterface {

    /**
     * Start main loop.
     */
    @PostConstruct
    fun start() {
        thread {
            logger.info("Start main loop.")
            runBlocking(Dispatchers.IO) {
                while (true) {
                    try {
                        launch {
                            createEvent()
                        }
                    } catch (e: RandomStringServiceException) {
                        logger.error("Caught exception while create new event. ${e.message}", e)
                    }
                    delay(5000)
                }
            }
        }
    }

    @Traced
    override suspend fun createEvent() {
        logger.info { "Scheduler triggered create new ChangeEvent." }
        createEventService.createEvent()
    }
}

interface EventCreateJobInterface {
    suspend fun createEvent()
}