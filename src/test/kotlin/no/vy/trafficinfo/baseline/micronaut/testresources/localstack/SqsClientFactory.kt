package no.vy.trafficinfo.tiosadapter.system.factories

import io.micronaut.context.annotation.ConfigurationBuilder
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.net.URI

@Factory
class SqsClientFactory {

    @Inject
    lateinit var sqsConfig: SQSConfig

    /**
     * Build SNS client for test.
     * Using the localstack endpoint and credentials from the application.yml
     */
    @Singleton
    @Primary
    fun buildSqsClient(): SqsClient {
        return SqsClient.builder()
            .region(Region.of(sqsConfig.region))
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .endpointOverride(URI.create("http://${sqsConfig.sqs.endpointOverride}:${sqsConfig.sqs.portOverride}"))
            .build()
    }

    /**
     * Config holder for the AWS configuration from
     * application.yml or the distributed configuration server.
     */
    @ConfigurationProperties("aws")
    class SQSConfig {
        var region: String? = "us-east-1"

        @ConfigurationBuilder(configurationPrefix = "services.sqs")
        var sqs: SQS = SQS()

        class SQS {
            var endpointOverride: String? = null
            var portOverride: String? = null
        }
    }
}