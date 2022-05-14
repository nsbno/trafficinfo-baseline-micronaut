package no.vy.trafficinfo.baseline.micronaut.domain

import io.micronaut.context.event.ApplicationEventPublisher
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.services.RandomStringService
import jakarta.inject.Singleton
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicLong

private const val MAX_SIZE = 10

private val logger = KotlinLogging.logger {}

/**
 *
 */
@Singleton
class ChangeEventRepository(
    private val randomStringService: RandomStringService,
    private val eventPublisher: ApplicationEventPublisher<ChangeEvent>
) {

    /* Used as a version count to number the generated updates */
    private val counter = AtomicLong()

    /* hold last 100 generated random string in memory */
    private val buffer = ArrayBlockingQueue<ChangeEvent>(MAX_SIZE)

    /**
     * ## Create new ChangeEvent and broadcast create event.
     */
    fun create(): ChangeEvent {
        val changeEvent = ChangeEvent(
            randomStringService.randomString(),
            counter.incrementAndGet()
        )

        if (buffer.size == MAX_SIZE) {
            logger.info { "Remove old ChangeEvent from repo before adding new." }
            buffer.remove()
        }

        logger.info { "Create new ChangeEvent $changeEvent" }
        buffer.add(changeEvent)
        eventPublisher.publishEventAsync(changeEvent)
        return changeEvent
    }

    /**
     * ## Return all values stored in buffer.
     */
    fun all() = buffer.toList()
}