package no.vy.trafficinfo.baseline.micronaut.domain

import io.micronaut.context.event.ApplicationEventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.suspendCancellableCoroutine
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.services.RandomStringService
import no.vy.trafficinfo.baseline.micronaut.services.RandomStringServiceException
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
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
    suspend fun create(): ChangeEvent

    /**
     * ## Return all values stored in buffer.
     */
    suspend fun all(): Flow<ChangeEvent>

    /**
     * ## Return number of events stored in buffer.
     */
    suspend fun count(): Int
    suspend fun clear()
}

/**
 * # Array backed in-memory repo.
 * TODO Add more docu.
 */
@Singleton
open class ChangeEventRepositoryImpl(
    private val dynamoDbClient: DynamoDbClient,
    private val randomStringService: RandomStringService,
    private val eventPublisher: ApplicationEventPublisher<ChangeEvent>
) : ChangeEventRepository {

    /* Used as a version count to number the generated updates */
    private val counter = AtomicLong()

    /* hold last 100 generated random string in memory */
    private val buffer = ArrayBlockingQueue<ChangeEvent>(MAX_SIZE)

    override suspend fun all(): Flow<ChangeEvent> {
        logger.info("Retrieve all events in buffer: ${buffer.size}")
        return buffer.asFlow().take(10)
    }

    override suspend fun count(): Int {
        return buffer.size
    }

    override suspend fun clear() {
        buffer.clear()
    }

    /**
     * ## Create new ChangeEvent.
     * Will also broadcast the newly created event on Micronaut
     * internal eventbus to notify observers.
     */
    override suspend fun create(): ChangeEvent {
        val randomString = requestRandomString()
        val changeEvent = ChangeEvent(
            randomString,
            counter.incrementAndGet()
        )

        if (buffer.size == MAX_SIZE) {
            logger.info { "Remove old ChangeEvent from repo before adding new." }
            buffer.remove()
        }

        logger.info { "Create new ChangeEvent $changeEvent" }
        buffer.add(changeEvent)
        dynamoDbClient.putItem {
            it.tableName("change-events")
            it.item(
                mapOf(
                    "version" to AttributeValue.builder().n(changeEvent.version.toString()).build(),
                    "payload" to AttributeValue.builder().s(changeEvent.payload).build(),
                    "ttl" to AttributeValue.builder().n("${System.currentTimeMillis()+10000}").build()
                )
            )
        }
        eventPublisher.publishEventAsync(changeEvent)
        return changeEvent
    }

    /**
     * ## Request Random String.
     *
     * Named "Request" to fake the apperance of a
     * remote system that generate the random string.
     */
    private suspend fun requestRandomString(): String {
        val randomString = suspendCancellableCoroutine { cont ->
            // simulate 250ms response time to create new random string,
            // as if the change event was generated for example from
            // and external system.
            Thread.sleep(250)
            if (true) {
                cont.resume(randomStringService.randomString())
            } else {
                cont.resumeWithException(RandomStringServiceException())
            }
        }
        return randomString
    }
}