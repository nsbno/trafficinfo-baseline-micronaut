package no.vy.trafficinfo.baseline.micronaut

import io.micronaut.discovery.event.ServiceReadyEvent
import io.micronaut.runtime.event.annotation.EventListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton

/**
 * This class listens for the ServiceReadyEvent.
 *
 * It can be used to print debug info of the started application,
 * maybe print some configuration values to the log so that its
 * easy to see important values.
 *
 * Can also be used to initialize the application with data
 * if you need to call out to external systems after the
 * application has started to pre-load cache or similary.
 */
@Singleton
class ApplicationInitializer {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationInitializer::class.java)
    }

    /**
     * Listen for the application has started even.
     */
    @EventListener
    fun applicationStartedUp(serviceStartedEvent: ServiceReadyEvent) {
        // add something to happen after ServiceReadyEvent here.
    }
}