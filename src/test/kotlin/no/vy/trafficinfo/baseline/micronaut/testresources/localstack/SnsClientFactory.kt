package no.vy.trafficinfo.tiosadapter.system.factories

import io.micronaut.context.annotation.ConfigurationBuilder
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.net.URI

@Factory
class SnsClientFactory {

    @Inject
    lateinit var snsConfig: SNSConfig

    /**
     * Build SNS client for test.
     * Using the localstack endpoint and credentials from the application.yml
     */
    @Singleton
    @Primary
    fun buildSnsClient(): SnsClient {
        return SnsClient.builder()
            .region(Region.of(snsConfig.region))
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .endpointOverride(URI.create("http://${snsConfig.sns.endpointOverride}:${snsConfig.sns.portOverride}"))
            .build()
    }

    /**
     * Config holder for the AWS configuration from
     * application.yml or the distributed configuration server.
     */
    @ConfigurationProperties("aws")
    class SNSConfig {
        var region: String? = "us-east-1"

        @ConfigurationBuilder(configurationPrefix = "services.sns")
        var sns: SNS = SNS()

        class SNS {
            var endpointOverride: String? = null
            var portOverride: String? = null
        }
    }
}