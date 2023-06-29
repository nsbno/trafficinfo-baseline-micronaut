package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.kotest.core.spec.style.BehaviorSpec
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEventRepository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

/**
 * ## Tests for ChangeEventRepository.
 */
@MicronautTest
class ChangeEventRepositorySpec(
    val dynamoDbClient: DynamoDbClient,
    val repository: ChangeEventRepository,
) : BehaviorSpec({

    beforeSpec {
    }

    afterSpec {
    }

    given("the health check endpoint") {
    }
})