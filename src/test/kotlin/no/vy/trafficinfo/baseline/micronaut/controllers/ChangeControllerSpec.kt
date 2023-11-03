package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEvent
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEventRepository
import no.vy.trafficinfo.baseline.micronaut.services.CreateEventService
import jakarta.inject.Inject

private val logger = KotlinLogging.logger {}

/**
 * # Declarative HTTP client for the ChangeController.
 *
 * Used to generate a client to communicate with the
 * controller from the Unit Test.
 */
@Client("/")
interface ChangeClient {

    @Get(value = "/changes")
    @Consumes(MediaType.APPLICATION_JSON_STREAM)
    fun changeEventUpdates(): Flow<ChangeEvent>

    @Get(value = "/changes")
    @Consumes(MediaType.APPLICATION_JSON)
    suspend fun changeEventsAll(): List<ChangeEvent>

    @Post(value = "/changes")
    @Consumes(MediaType.APPLICATION_JSON)
    suspend fun changeEventCreate(): ChangeEvent
}

/**
 * https://codingwithmohit.com/micronaut/micronaut-with-kotlin-coroutines/
 * https://www.baeldung.com/kotlin/kotest
 * https://docs.micronaut.io/latest/guide/#coroutines
 * https://kotest.io/
 */
@MicronautTest
class ChangeControllerSpec(
    @Inject val createEventService: CreateEventService,
    @Inject val client: ChangeClient,
    @Inject val changeEventRepository: ChangeEventRepository
) : BehaviorSpec({

    given("the 5 generated events") {
        (1..5).forEach {
            createEventService.createEvent()
        }

        `when`("the streaming changes endpoint is called") {
            val list = client.changeEventUpdates()
                .take(3)
                .toCollection(mutableListOf())

            then("the result should be three items") {
                list.shouldHaveSize(3)
            }
        }

        `when`("the blocking changes endpoint is called") {
            val list = client.changeEventsAll()

            then("the result should be all items") {
                list.shouldHaveSize(5)
            }
        }
    }

    given("an empty repository") {
        changeEventRepository.clear()

        `when`("the create endpoint is called") {
            val event = client.changeEventCreate()

            then("repository should contain one event") {
                changeEventRepository.count().shouldBe(1)
            }
        }
    }
})