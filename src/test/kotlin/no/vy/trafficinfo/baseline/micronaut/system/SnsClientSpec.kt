package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.kotest.assertions.until.fixed
import io.kotest.assertions.until.until
import io.kotest.core.spec.style.BehaviorSpec
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.ObjectMother
import no.vy.trafficinfo.baseline.micronaut.ObjectMother.Companion.TEST_TOPIC_ARN
import software.amazon.awssdk.services.sns.SnsClient
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
    @Inject var messageConsumer: TextMessageConsumer
) : BehaviorSpec({

    /**
     * Create queue and topic before running tests.
     */
    beforeSpec {
        logger.info("Before spec.")
    }

    /**
     *
     */
    afterSpec {
        logger.info("After spec.")
    }

    /**
     * Perform some testing here.
     */
    given("some test data") {
        val testMessage = "a test message with text"

        `when`("calling something") {
            snsClient.publish {
                with(it){
                    topicArn(TEST_TOPIC_ARN)
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