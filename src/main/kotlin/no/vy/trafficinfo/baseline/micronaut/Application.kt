package no.vy.trafficinfo.baseline.micronaut

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
    private val log: Logger = LoggerFactory.getLogger(Application::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
// FIXME: 08.07.2021 this should be configurable by args or system properties
// to be toggleable externally on / off.
//      initAwsCredentials()

        Micronaut.build()
            .packages("no.vy.trafficinfo.baseline.micronaut")
            .mainClass(Application.javaClass)
            .start()
    }

    /**
     * Set AWS Credentials Profile to be used when doing local development.
     * This will enable the service to use your local credentials to gain
     * access to the AWS account that you have logged into.
     *
     * It will still fail if the service accesses databases and such that
     * reside in the internal VPC in the account that has no external access.
     */
    private fun initAwsCredentials() {
        val awsProfile = "dev"
        System.setProperty("aws.profile", awsProfile)
        log.info("AWS profile $awsProfile set")
    }

}