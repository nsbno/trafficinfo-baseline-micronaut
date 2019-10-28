package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.net.URI
import javax.inject.Inject

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HealthControllerTest {
    @Inject
    lateinit var server: EmbeddedServer

    private lateinit var client : HttpClient

    @BeforeAll
    fun init() {
        client = HttpClient.create(server.url)
    }

    @Test
    fun testHealthResponse() {
        val rsp: String = client.toBlocking().retrieve("/health")
        assertTrue(rsp.contains("runningSince"))
    }

    @Test
    fun testSecuredHealthResponseWithoutAuth() {
        assertThrows<HttpClientResponseException> {
            client.toBlocking().retrieve("/secured/health")
        }
    }

    @Test
    fun testSecuredHealthResponseWithAuth() {
        val req : HttpRequest<Health> = HttpRequest.GET<Health>(URI.create("/secured/health")).basicAuth("user", "password")
        val rsp : Health = client.toBlocking().retrieve(req, Health::class.java)

        assertNotNull(rsp.service)
        assertNotNull(rsp.now)
        assertNotNull(rsp.runningSince)
        assertNotNull(rsp.version)
    }
}