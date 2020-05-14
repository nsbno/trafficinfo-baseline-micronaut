package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import java.net.URI
import javax.inject.Inject
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HealthControllerTest {
    @Inject
    lateinit var server: EmbeddedServer
    lateinit var client: HttpClient

    @BeforeAll
    fun init() {
        client = HttpClient.create(server.url)
    }

    @Test
    fun testHealthResponse() {
        val rsp: String = client.toBlocking().retrieve("/health")
        assertTrue(rsp.contains("startupTime"))
    }

    @Test
    fun testSecuredHealthResponseWithoutAuth() {
        assertThrows<HttpClientResponseException> {
            client.toBlocking().retrieve("/secured/health")
        }
    }

    @Test
    @Disabled
    fun testSecuredHealthResponseWithAuth() {
        val req: HttpRequest<Health> = HttpRequest.GET<Health>(URI.create("/secured/health")).basicAuth("user", "password")
        val rsp: Health = client.toBlocking().retrieve(req, Health::class.java)

        assertNotNull(rsp.service)
        assertNotNull(rsp.now)
        assertNotNull(rsp.runningSince)
        assertNotNull(rsp.version)
    }
}