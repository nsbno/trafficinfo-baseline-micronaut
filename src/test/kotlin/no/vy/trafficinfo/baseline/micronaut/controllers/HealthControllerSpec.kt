package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.context.ApplicationContext
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object HealthControllerSpec: Spek({
    describe("HealthController Suite") {
        var embeddedServer : EmbeddedServer = ApplicationContext.run(EmbeddedServer::class.java)
        var client : HttpClient = HttpClient.create(embeddedServer.url)

        it("test /health contains startuptime") {
            var rsp : String = client.toBlocking().retrieve("/health")
            assertTrue(rsp.contains("runningSince"))
        }

        afterGroup {
            client.close()
            embeddedServer.close()
        }
    }
})