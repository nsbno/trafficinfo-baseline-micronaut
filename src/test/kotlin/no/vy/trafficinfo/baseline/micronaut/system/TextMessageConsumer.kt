package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.jms.annotations.JMSListener
import io.micronaut.jms.annotations.Queue
import io.micronaut.jms.sqs.configuration.SqsConfiguration.CONNECTION_FACTORY_BEAN_NAME
import io.micronaut.messaging.annotation.MessageBody
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@JMSListener(CONNECTION_FACTORY_BEAN_NAME)
class TextMessageConsumer {

    val processed: MutableList<String> = mutableListOf()

    init {
        logger.info("TextMessageConsumer created")
    }

    @Queue(value = "test-queue")
    fun receive(@MessageBody body: String) {
        logger.info("TextMessageConsumer received: $body")
        processed.add(body)
    }
}