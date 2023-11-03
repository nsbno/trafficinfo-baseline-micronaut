package no.vy.trafficinfo.baseline.micronaut.system.events

import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.ObjectMother.Companion.TEST_QUEUE_NAME
import software.amazon.awssdk.services.sqs.SqsClient
import jakarta.annotation.Priority
import jakarta.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * This class listens for the SqsClient bean being created.
 * It will be called after the bean has been created and
 * will create the test queue if it does not exist.
 */
@Singleton
@Priority(1)
class SqsClientCreatedEventListener : BeanCreatedEventListener<SqsClient> {
    override fun onCreated(event: BeanCreatedEvent<SqsClient>): SqsClient {
        val client = event.bean
        if (client.listQueues().queueUrls().find { it.endsWith(TEST_QUEUE_NAME) } == null) {
            // create a queue and a topic and subscribe the queue to the topic
            val queueUrl = client.createQueue {
                it.queueName(TEST_QUEUE_NAME)
            }.queueUrl()
            logger.info("Created queue $queueUrl")
        }

        return client
    }
}