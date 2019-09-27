package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class HealthControllerTest {
    @Inject
    lateinit var server: EmbeddedServer

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun testHealthResponse() {
        val rsp: String = client.toBlocking().retrieve("/health")
        assertTrue(rsp.contains("runningSince"))
    }
}