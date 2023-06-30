package no.vy.trafficinfo.baseline.micronaut.domain

import kotlinx.coroutines.flow.Flow

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