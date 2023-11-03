package no.vy.trafficinfo.baseline.micronaut

/**
 * This class contains constants that can be used in tests.
 * It is a companion object so that you can use the constants
 * without having to write the class name.
 *
 * Example:
 *  ```
 *  import no.vy.trafficinfo.baseline.micronaut.ObjectMother
 *  ...
 *  val queueName = TEST_QUEUE_NAME
 *  ```
 */
class ObjectMother {
    companion object {
        const val TEST_QUEUE_NAME = "test-queue"
        const val TEST_TOPIC_NAME = "test-topic"
        const val TEST_TOPIC_ARN = "arn:aws:sns:us-east-1:000000000000:$TEST_TOPIC_NAME"
        const val TEST_QUEUE_ARN = "arn:aws:sns:us-east-1:000000000000:$TEST_QUEUE_NAME"
    }
}