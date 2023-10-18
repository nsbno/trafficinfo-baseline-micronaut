package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.kotest.assertions.until.fixed
import io.kotest.assertions.until.until
import io.kotest.core.spec.style.BehaviorSpec
import mu.KotlinLogging
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.QueueAttributeName
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import jakarta.inject.Inject

private val logger = KotlinLogging.logger {}

/**
 * This class shows how to use the SNS client in a Unit Test using
 * the `test-resources` feature in Micronaut and LocalStack to run
 * a local version of SNS, SQS during the unit test.
 *
 * It will create a queue and a topic and subscribe the queue to the topic
 * to publish some messages and read them.
 *
 * To read from the SQS queue, the test will use the SQS client from
 * the Micronaut JMS Messaging library that supports SQS as well as JMS.
 *
 */
@MicronautTest(startApplication = false)
class SnsClientSpec(
    @Inject var snsClient: SnsClient,
    @Inject var sqsClient: SqsClient,
    @Inject var messageConsumer: TextMessageConsumer
) : BehaviorSpec({

    /**
     * Create queue and topic before running tests.
     */
    beforeSpec {
        val queueUrl = sqsClient.getQueueUrl {
            it.queueName("test-queue")
        }.queueUrl()

        val subResp = snsClient.subscribe {
            it.topicArn("arn:aws:sns:us-east-1:000000000000:test-topic")
            it.protocol("sqs")
            it.endpoint("arn:aws:sqs:us-east-1:000000000000:test-queue")
        }
        logger.info("Created subscription: $subResp")
    }

    /**
     *
     */
    afterSpec {

    }

    /**
     * Perform some testing here.
     */
    given("some test data") {
        val testMessage = "a test message with text"

        `when`("calling something") {
            snsClient.publish {
                with(it){
                    topicArn("arn:aws:sns:us-east-1:000000000000:test-topic")
                    message(testMessage)
                }
            }

            then("we should get a result") {
                until(5.seconds, 250.milliseconds.fixed()){
                    messageConsumer.processed.size==1
                }
            }
        }
    }
})