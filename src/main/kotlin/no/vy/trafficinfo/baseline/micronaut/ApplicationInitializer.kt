package no.vy.trafficinfo.baseline.micronaut

import io.micronaut.discovery.event.ServiceReadyEvent
import io.micronaut.discovery.event.ServiceStoppedEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.micrometer.core.instrument.MeterRegistry
import no.vy.trafficinfo.baseline.micronaut.routes.DummyRouteBuilder
import org.apache.camel.main.Main
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Application Initializer that starts Apache Camel.
 * This class listens for the ServiceReadyEvent and ServiceStoppedEvent.
 * When receiving the events it will start or stop the Apache Camel
 * running inside Micronaut.
 */
@Singleton
class ApplicationInitializer {

    @Inject
    lateinit var myRouteBuilder: DummyRouteBuilder

    @Inject
    lateinit var metrics: MeterRegistry

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationInitializer::class.java)
        private val main = Main()
    }

    /**
     * Listen for the application has started event and starts Apache Camel.
     */
    @EventListener
    fun applicationStartedUp(serviceStartedEvent: ServiceReadyEvent) {
        main.bind("meterRegistry", metrics)
        main.configure().addRoutesBuilder(myRouteBuilder)
        main.start()
    }

    /**
     * Listen for the application has shutdown event to stop Camel gracefully.
     */
    @EventListener
    fun applicationShutdown(serviceStoppedEvent: ServiceStoppedEvent) {
        main.shutdown()
    }
}