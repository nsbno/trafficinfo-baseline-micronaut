package no.vy.trafficinfo.baseline.micronaut.routes

import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.spi.ThreadPoolProfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import jakarta.inject.Singleton
import java.util.concurrent.TimeUnit

@Singleton
class DummyRouteBuilder : RouteBuilder() {

    @Throws(Exception::class)
    override fun configure() {
        val poolProfile = ThreadPoolProfile("masterPoolProfile")
        poolProfile.maxPoolSize = 100
        poolProfile.maxQueueSize = 100
        poolProfile.poolSize = 100
        poolProfile.keepAliveTime = 1
        poolProfile.timeUnit = TimeUnit.MINUTES
        context.executorServiceManager.defaultThreadPoolProfile = poolProfile

        onException()
            .handled(true)
            .maximumRedeliveries(2)
            .logStackTrace(false)
            .logExhausted(false)
            .log(LoggingLevel.ERROR, "Failed processing \${body}")

        from("direct:start")
            .routeId("dummy-route")
            .to("log:no.vy.trafficinfo.baseline?level=INFO&groupInterval=5000&groupDelay=5000&groupActiveOnly=false")
            .to("micrometer:timer:timer?action=start")
            .bean(SomeBean())
            .to("micrometer:timer:timer?action=stop");
    }

    /**
     * Just a dummy bean to receive messages.
     */
    class SomeBean {
        companion object {
            private val log: Logger = LoggerFactory.getLogger(SomeBean::class.java)
        }

        fun handle(exchange: Exchange) {
            log.debug("Received: ${exchange.`in`.body}")
        }
    }
}