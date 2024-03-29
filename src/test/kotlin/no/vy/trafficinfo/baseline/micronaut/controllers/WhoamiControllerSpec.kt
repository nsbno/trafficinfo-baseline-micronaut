package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * ## Tests for Health Checks controller.
 */
@MicronautTest
class WhoamiControllerSpec(
    @Client("whoami") val client: HttpClient
) : BehaviorSpec({

    xgiven("the health check endpoint") {
        `when`("calling the health check endpoint") {
            val result = client.toBlocking().exchange<String>("/health")

            `then`("we should get successful http status in return") {
                result.status.shouldBe(HttpStatus.OK)
            }
        }
    }
})