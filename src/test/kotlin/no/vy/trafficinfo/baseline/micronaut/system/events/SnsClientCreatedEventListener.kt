package no.vy.trafficinfo.baseline.micronaut.system.events

import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.ObjectMother.Companion.TEST_QUEUE_ARN
import no.vy.trafficinfo.baseline.micronaut.ObjectMother.Companion.TEST_TOPIC_ARN
import no.vy.trafficinfo.baseline.micronaut.ObjectMother.Companion.TEST_TOPIC_NAME
import software.amazon.awssdk.services.sns.SnsClient
import jakarta.annotation.Priority
import jakarta.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * This class listens for the SnsClient bean being created.
 * It will be called after the bean has been created and
 * will create the test topic if it does not exist.
 *
 * Also, it will create a subscription to the test queue.
 * It's important to note that the test queue must be created
 * before the test topic is created, otherwise the subscription
 * will fail. This is why the SqsClientCreatedEventListener
 * is annotated with @Priority(1) and this class is annotated
 *
 */
@Singleton
@Priority(2)
class SnsClientCreatedEventListener : BeanCreatedEventListener<SnsClient> {
    override fun onCreated(event: BeanCreatedEvent<SnsClient>): SnsClient {
        val client = event.bean
        if (client.listTopics().topics().find { it.topicArn().endsWith(TEST_TOPIC_NAME) } == null) {
            val topicArn = client.createTopic {
                it.name(TEST_TOPIC_NAME)
            }.topicArn()

            val subResp = client.subscribe {
                it.topicArn(TEST_TOPIC_ARN)
                it.protocol("sqs")
                it.endpoint(TEST_QUEUE_ARN)
            }
            logger.info("Created topic $topicArn")
            logger.info("Created subscription: $subResp")
        }

        return client
    }
}