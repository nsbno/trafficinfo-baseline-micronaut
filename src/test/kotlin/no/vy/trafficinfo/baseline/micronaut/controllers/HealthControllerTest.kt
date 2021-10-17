package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.jwt.validator.JwtTokenValidator
import io.micronaut.security.token.validator.TokenValidator
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Flux
import jakarta.inject.Inject
import java.net.URI
import java.util.UUID

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HealthControllerTest {

    @MockBean(JwtTokenValidator::class)
    fun tokenvalidator(): JwtTokenValidator {
        return mockk()
    }

    @Inject
    lateinit var tokenValidator: TokenValidator

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

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
    fun testSecuredHealthResponseWithBasicAuth() {
        val req: HttpRequest<Health> = HttpRequest.GET<Health>(URI.create("/secured/health")).basicAuth("user", "password")
        val rsp: Health = client.toBlocking().retrieve(req, Health::class.java)

        assertNotNull(rsp.service)
        assertNotNull(rsp.now)
        assertNotNull(rsp.runningSince)
        assertNotNull(rsp.version)
    }

    @Test
    fun testSecuredHealthResponseWithBearerAuth() {
        every {
            tokenValidator.validateToken(or(ofType(String::class), isNull()), ofType(HttpRequest::class))
        } returns Flux.just(Authentication.build("user", listOf("https://services.trafficinfo.vydev.io/baseline-micronaut/read"), emptyMap()))

        val req: HttpRequest<Health> = HttpRequest.GET<Health>(URI.create("/secured/health")).bearerAuth(UUID.randomUUID().toString())
        val rsp: Health = client.toBlocking().retrieve(req, Health::class.java)

        assertNotNull(rsp.service)
        assertNotNull(rsp.now)
        assertNotNull(rsp.runningSince)
        assertNotNull(rsp.version)
    }
}