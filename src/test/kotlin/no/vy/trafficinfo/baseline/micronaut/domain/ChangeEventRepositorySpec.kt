package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEventRepository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

private val logger = KotlinLogging.logger {}

/**
 * ## Tests for ChangeEventRepository.
 */
@MicronautTest(startApplication = false)
class ChangeEventRepositorySpec(
    val dynamoDbClient: DynamoDbClient,
    val repository: ChangeEventRepository,
) : BehaviorSpec({

    /**
     * Create database table before running tests.
     */
    beforeSpec {
        logger.debug { "Creating DynamoDB table in localstack." }

        dynamoDbClient.createTable {
            it.tableName("change-events")
            it.keySchema({
                it.keyType("HASH")
                it.attributeName("version")
            })
            it.attributeDefinitions(
                {
                    it.attributeName("version")
                    it.attributeType("N")
                }
            )
            it.provisionedThroughput {
                it.readCapacityUnits(1)
                it.writeCapacityUnits(1)
            }

            dynamoDbClient.listTables().tableNames().forEach {
                logger.debug { "Found table: $it" }
            }
        }
    }

    /**
     * Cleanup database table after running tests.
     */
    afterSpec {
        dynamoDbClient.deleteTable {
            logger.debug { "Deleting DynamoDB \"change-events\" table in localstack." }
            it.tableName("change-events")
        }
    }

    given("a repository") {
        `when`("creating new events") {
            val newEvent = repository.create()

            `then`("we should have new events in the database table") {
                newEvent.version shouldNotBe null
            }

            `and`("we should be able to retrieve the new event") {
                val retrievedEvent = repository.all().firstOrNull()
                retrievedEvent shouldNotBe null
                retrievedEvent shouldBe newEvent
            }
        }
    }
})