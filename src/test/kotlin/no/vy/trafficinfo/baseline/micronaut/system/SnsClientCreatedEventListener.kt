package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import mu.KotlinLogging
import software.amazon.awssdk.services.sns.SnsClient
import jakarta.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class SnsClientCreatedEventListener : BeanCreatedEventListener<SnsClient> {
    override fun onCreated(event: BeanCreatedEvent<SnsClient>): SnsClient {
        val client = event.bean
        logger.info { "SnsClientCreatedEventListener.onCreated: $client" }
        val topicArn = client.createTopic {
            it.name("test-topic")
        }.topicArn()

        logger.info("Created topic $topicArn")
        return client
    }
}