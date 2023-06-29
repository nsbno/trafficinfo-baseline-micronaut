package no.vy.trafficinfo.baseline.micronaut.domain

import io.micronaut.context.annotation.Value
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

@MicronautTest
class ChangeEventRepositoryImplTest extends Specification {

    @Inject
    @Shared
    DynamoDbClient dynamoDbClient

    @Subject
    @Inject
    ChangeEventRepository repository

    @Value('${aws.services.dynamodb.endpoint-override}')
    String endpointOverride

    /**
     * Create the table before the test is run.
     * @return
     */
    def setupSpec() {
        try {
            println "Creating DynamoDB table in localstack."
            dynamoDbClient.createTable {
                it.tableName("change_event")
                it.keySchema(KeySchemaElement.builder()
                    .attributeName("id")
                    .keyType("HASH")
                    .build())
                it.attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType("S")
                        .build()
                )
                it.provisionedThroughput {
                    it.readCapacityUnits(1)
                    it.writeCapacityUnits(1)
                }
            }

            dynamoDbClient.listTables().tableNames().forEach {
                println "Table created: $it."
            }
        } catch (AwsServiceException | SdkClientException e) {
            println "Error while creating DynamoDB connection during init phase: ${e.message}"
        }
    }

    /**
     * Delete the table after the test is run.
     * @return
     */
    def cleanupSpec() {
        try {
            dynamoDbClient.listTables().tableNames().forEach {
                dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName(it).build())
                println "Table $it deleted."
            }
        } catch (AwsServiceException | SdkClientException e) {
            println "Error while creating DynamoDB connection during cleanup phase: ${e.message}"
        }
    }

    /**
     */
    def "should create new event"() {
        when:
        def event = repository.create()

        then:
        event != null

        and:
        dynamoDbClient.scan {
            it.tableName("change_event")
        }.items()[0].get("id").s() == event.version.toString()
    }
}
