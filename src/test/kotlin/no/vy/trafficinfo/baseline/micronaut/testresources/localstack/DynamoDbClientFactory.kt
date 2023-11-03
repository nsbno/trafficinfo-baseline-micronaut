package no.vy.trafficinfo.baseline.micronaut.testresources.localstack

import io.micronaut.context.annotation.ConfigurationBuilder
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.net.URI

@Factory
class DynamoDbClientFactory {

    @Inject
    lateinit var dynamodbConfig: DynamoDbConfig

    /**
     * Build dynamodb client for test.
     * Using the localstack endpoint and credentials from the application.yml
     */
    @Singleton
    @Primary
    fun buildDynamoDbClient(): DynamoDbClient {
        return DynamoDbClient.builder()
            .region(Region.of(dynamodbConfig.region))
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .endpointOverride(URI.create("${dynamodbConfig.dynamodb.endpointOverride}:${dynamodbConfig.dynamodb.portOverride}"))
            .build()
    }

    /**
     * Config holder for the AWS configuration from
     * application.yml or the distributed configuration server.
     */
    @ConfigurationProperties("aws")
    class DynamoDbConfig {
        var region: String? = "us-east-1"

        @ConfigurationBuilder(configurationPrefix = "services.dynamodb")
        var dynamodb = DynamoDB()

        class DynamoDB {
            var endpointOverride: String? = null
            var portOverride: String? = null
        }
    }
}