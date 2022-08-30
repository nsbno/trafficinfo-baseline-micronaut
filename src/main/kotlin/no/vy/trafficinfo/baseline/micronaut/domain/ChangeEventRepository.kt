package no.vy.trafficinfo.baseline.micronaut.domain

import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.tracing.annotation.NewSpan
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.services.RandomStringService
import reactor.core.publisher.Flux
import jakarta.inject.Singleton
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicLong

private const val MAX_SIZE = 10

private val logger = KotlinLogging.logger {}

/**
 * # Repo holding ChangeEvents.
 * TODO Add more Docu
 */
interface ChangeEventRepository {
    /**
     * ## Create new ChangeEvent and broadcast create event.
     */
    fun create(): ChangeEvent

    /**
     * ## Return all values stored in buffer.
     */
    fun all(): Flux<ChangeEvent>?
}

/**
 * # Array backed in-memory repo.
 * TODO Add more docu.
 */
@Singleton
open class ChangeEventRepositoryImpl(
    private val randomStringService: RandomStringService,
    private val eventPublisher: ApplicationEventPublisher<ChangeEvent>
) : ChangeEventRepository {

    /* Used as a version count to number the generated updates */
    private val counter = AtomicLong()

    /* hold last 100 generated random string in memory */
    private val buffer = ArrayBlockingQueue<ChangeEvent>(MAX_SIZE)

    override fun create(): ChangeEvent {
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

    @NewSpan
    override fun all() = Flux.fromIterable(buffer)
}