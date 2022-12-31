package no.vy.trafficinfo.baseline.micronaut.system.clients

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import kotlinx.coroutines.flow.Flow
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEvent

/**
 * # Interface for the controller endpoints.
 *
 * Used to generate client to communicate with the
 * controller from the Unit Test.
 */
@Client("baseline")
interface BaselineClient {

    /**
     * ## Return a stream of events.
     *
     * The controller generates one event every second.
     * The annotation @Consumes sets the accept-type
     * on the request so that the client calls the
     * correct endpoint.
     */
    @Get(value = "/changes")
    @Consumes(MediaType.APPLICATION_JSON_STREAM)
    fun changeEventUpdates(): Flow<ChangeEvent>

    /**
     * ## Return all records from repository.
     */
    @Get(value = "/changes")
    @Consumes(MediaType.APPLICATION_JSON)
    suspend fun changeEventsAll(): List<ChangeEvent>

    /**
     * ## Create and return a single event.
     */
    @Post(value = "/changes")
    @Consumes(MediaType.APPLICATION_JSON)
    suspend fun changeEventCreate()
}

