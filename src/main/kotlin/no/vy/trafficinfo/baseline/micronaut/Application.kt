package no.vy.trafficinfo.baseline.micronaut

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info

/**
 * Main application object
 * Started by Docker container.
 */
@OpenAPIDefinition(
        info = Info(
                title = "Micronaut Baseline",
                version = "0.1",
                description = "Trafficinfo baseline for microservices implemented with Micronaut",
                contact = Contact(url = "https://vy.no", name = "Marius Gravdal", email = "marius.aune.gravdal@vy.no")
        )
)
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("no.vy.trafficinfo.baseline.micronaut")
                .mainClass(Application.javaClass)
                .start()
    }
}