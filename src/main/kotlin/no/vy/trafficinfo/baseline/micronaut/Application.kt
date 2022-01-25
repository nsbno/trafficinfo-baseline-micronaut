package no.vy.trafficinfo.baseline.micronaut

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Main application object
 * Started by Docker container.
 */
@OpenAPIDefinition(
    info = Info(
        title = "Micronaut Baseline",
        version = "1.0",
        description = "Micronaut Baseline project used as tempalate for new microservices.",
        contact = Contact(url = "https://vy.no", name = "Daniel Engfeldt", email = "daniel.engfeldt@vy.no")
    ),
    servers = [
        Server(url = "https://services.trafficinfo.vydev.io/micronaut-baseline", description = "production"),
        Server(url = "https://services.stage.trafficinfo.vydev.io/micronaut-baseline", description = "stage"),
        Server(url = "https://services.test.trafficinfo.vydev.io/micronaut-baseline", description = "test"),
        Server(url = "https://services.dev.trafficinfo.vydev.io/micronaut-baseline", description = "dev")
    ],
    externalDocs = ExternalDocumentation(
        description = "Internal Application Documentation",
        url = "https://vygruppen.atlassian.net/wiki/spaces/TRAFFICINFO/pages/3793586330/Developer"
    )
)
@SecurityScheme(
    paramName = "Authorization",
    description = "Use Central Cognito to Authorize requests to microservice.",
    name = "cognito_auth",
    type = SecuritySchemeType.APIKEY,
    scheme = "bearer",
    `in` = SecuritySchemeIn.HEADER,
    extensions = [
        Extension(
            name = "",
            properties = [
                ExtensionProperty(name = "x-amazon-apigateway-authtype", value = "cognito_user_pools")
            ]
        ),
        Extension(
            name = "x-amazon-apigateway-authorizer",
            properties = [
                ExtensionProperty(name = "providerARNs", value = "[\"\${provider_arn}\"]", parseValue = true),
                ExtensionProperty(name = "type", value = "cognito_user_pools"),
            ]
        )
    ]
)
/**
 * ## Application
 * <p>
 * This microservice runs in Amazon ECS Fargate in the cloud.
 * </p>
 *
 * <p>
 * This service uses the default AWS Credentials Providers to authenticate
 * against AWS. While running as a container in ECS it will use the
 * ContainerCredentialsProvider and get the permissions configured
 * to the attached task role to the task.
 *
 * For local development profiles are very easy to use for authentication.
 * Set AWS Credentials Profile to be used when doing local development.
 * This will enable the service to use your local credentials to gain
 * access to the AWS account that you have logged into.
 * </p>
 *
 * @see https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
 */
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("no.vy.trafficinfo.baseline.micronaut")
            .mainClass(Application.javaClass)
            .banner(false)
            .start()
    }
}