package no.vy.trafficinfo.baseline.micronaut.jobs

import io.micronaut.core.annotation.Introspected
import io.micronaut.scheduling.annotation.Scheduled
import co.elastic.apm.api.ElasticApm
import co.elastic.apm.api.Transaction
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
    @Scheduled(fixedDelay = "10s")
    open fun createEvent() {
        val transaction: Transaction = ElasticApm.startTransaction()
        try {
            transaction.activate().use { scope ->
                logger.info { "Scheduler triggered create new ChangeEvent." }
                transaction.setName("Scheduler triggered create new ChangeEvent")
                transaction.setType(Transaction.TYPE_REQUEST)
                repo.create()
            }
        } catch (e: java.lang.Exception) {
            transaction.captureException(e)
            throw e
        } finally {
            transaction.end()
        }
    }
}