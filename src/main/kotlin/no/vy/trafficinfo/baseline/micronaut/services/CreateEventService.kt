package no.vy.trafficinfo.baseline.micronaut.services

import co.elastic.apm.api.Traced
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEvent
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEventRepository
import jakarta.inject.Named
import jakarta.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * ## Create a new change event in the repository.
 *
 * As a side effect the repository will broadcast
 * the newly created ChangeEvent on the internal
 * micronaut EventPublisher bus which the
 * ChangeController listens for.
 */
@Singleton
@Named("create_event")
class CreateEventServiceImpl(
    private val repo: ChangeEventRepository
) : CreateEventService {

    @Traced
    override suspend fun createEvent() = repo.create()
}

interface CreateEventService {
    suspend fun createEvent(): ChangeEvent
}