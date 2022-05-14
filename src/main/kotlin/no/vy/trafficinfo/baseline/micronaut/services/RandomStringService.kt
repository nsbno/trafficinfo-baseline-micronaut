package no.vy.trafficinfo.baseline.micronaut.services

import mu.KotlinLogging
import kotlin.random.Random
import jakarta.inject.Singleton

/* Place definition above class declaration to make field static */
private val logger = KotlinLogging.logger {}

/* length of random string */
private val STRING_LENGTH = 10

/* the allowed chars to generate random string from, */
private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

/**
 * # Generate a random string.
 */
@Singleton
class RandomStringService {

    /**
     * ## Generate a random string.
     */
    fun randomString() = (1..STRING_LENGTH)
        .map { i -> Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}