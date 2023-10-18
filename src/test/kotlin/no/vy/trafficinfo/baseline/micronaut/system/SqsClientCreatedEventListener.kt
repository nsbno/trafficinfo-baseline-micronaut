package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import mu.KotlinLogging
import software.amazon.awssdk.services.sqs.SqsClient
import jakarta.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class SqsClientCreatedEventListener : BeanCreatedEventListener<SqsClient> {
    override fun onCreated(event: BeanCreatedEvent<SqsClient>): SqsClient {
        val client = event.bean
        logger.info { "SqsClientCreatedEventListener.onCreated: $client" }
        // create a queue and a topic and subscribe the queue to the topic
        val queueUrl = client.createQueue {
            it.queueName("test-queue")
        }.queueUrl()

        logger.info("Created queue $queueUrl")
        return client
    }
}