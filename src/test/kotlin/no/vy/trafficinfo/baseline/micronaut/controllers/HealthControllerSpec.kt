package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import mu.KotlinLogging
import jakarta.inject.Inject

private val logger = KotlinLogging.logger {}

@Client("/")
interface HealthClient {
    @Get("/health")
    fun health(): HttpResponse<String>
}

/**
 * ## Tests for Health Checks controller.
 */
@MicronautTest
class HealthControllerSpec(
    @Inject val client: HealthClient) : BehaviorSpec({

    given("the health check endpoint") {
        `when`("calling the health check endpoint") {
            val result = client.health()

            `then`("we should get successful http status in return") {
                result.status.shouldBe(HttpStatus.OK)
            }
        }
    }
})
