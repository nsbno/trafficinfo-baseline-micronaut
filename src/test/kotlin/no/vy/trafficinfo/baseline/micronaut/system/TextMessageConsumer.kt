package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.jms.annotations.JMSListener
import io.micronaut.jms.annotations.Queue
import io.micronaut.jms.sqs.configuration.SqsConfiguration.CONNECTION_FACTORY_BEAN_NAME
import io.micronaut.messaging.annotation.MessageBody
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.ObjectMother.Companion.TEST_QUEUE_NAME

private val logger = KotlinLogging.logger {}

/**
 * This class listens for messages on the test queue.
 * It is used in tests to verify that messages are sent
 * to the queue.
 *
 * The @JMSListener annotation tells Micronaut to create
 * a message listener for the queue.
 *
 * @link https://micronaut-projects.github.io/micronaut-jms/latest/guide
 */
@JMSListener(CONNECTION_FACTORY_BEAN_NAME)
class TextMessageConsumer {

    // This list is used in tests to verify that messages are sent to the queue.
    val processed: MutableList<String> = mutableListOf()

    /**
     * This method is called when a message is received on the queue.
     * The @Queue annotation tells Micronaut which queue to listen to.
     *
     * @link https://micronaut-projects.github.io/micronaut-jms/latest/guide/#jmsListener
     */
    @Queue(value = TEST_QUEUE_NAME)
    fun receive(@MessageBody body: String) {
        logger.info("TextMessageConsumer received: $body")
        processed.add(body)
    }
}