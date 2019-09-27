package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.context.ApplicationContext
import io.micronaut.core.type.Argument
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import org.junit.jupiter.api.Assertions.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object HealthControllerSpec: Spek({
    describe("HealthController Suite") {
        var embeddedServer : EmbeddedServer = ApplicationContext.run(EmbeddedServer::class.java)
        var client : HttpClient = HttpClient.create(embeddedServer.url)

        it("test /health contains stuff") {
            var rsp : Health = client.toBlocking().retrieve("/health", Health::class.java)
            assertNotNull(rsp.now)
            assertNotNull(rsp.runningSince)
            assertEquals("micronaut-baseline", rsp.service)
            assertEquals("development", rsp.version)
        }

        afterGroup {
            client.close()
            embeddedServer.close()
        }
    }
})