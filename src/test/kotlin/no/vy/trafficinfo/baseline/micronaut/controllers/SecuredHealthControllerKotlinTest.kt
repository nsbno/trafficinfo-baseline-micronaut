package no.vy.trafficinfo.baseline.micronaut.controllers

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.BehaviorSpec
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import no.vy.trafficinfo.baseline.micronaut.Application
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import no.vy.trafficinfo.baseline.micronaut.services.UserAuthenticationService
import java.net.URI
import javax.inject.Inject

@MicronautTest(application = Application::class)
class SecuredHealthControllerKotlinTest (
        private val embeddedServer: EmbeddedServer
) : BehaviorSpec({
        given("the secured health endpoint") {
            val client : HttpClient = HttpClient.create(embeddedServer.url)
            `when`("the endpoint is called without auth") {
                then("it should throw an exception") {
                    shouldThrow<HttpClientResponseException> {
                        client.toBlocking().exchange<Health>("/secured/health")
                    }
                }
            }
            `when`("the endpoint is called with correct auth") {
                val req : HttpRequest<Health> = HttpRequest.GET<Health>(URI.create("/secured/health")).basicAuth("user", "password")
                val rsp : Health= client.toBlocking().retrieve(req, Health::class.java)
                then("the service should return with health info") {
                    rsp.service shouldBe "micronaut-baseline"
                    rsp.version shouldBe "development"
                    rsp.now shouldNotBe null
                    rsp.runningSince shouldNotBe null
                }
            }
        }
})