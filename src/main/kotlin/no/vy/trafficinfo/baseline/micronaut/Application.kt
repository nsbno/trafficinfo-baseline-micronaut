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
import io.swagger.v3.oas.annotations.security.OAuthFlow
import io.swagger.v3.oas.annotations.security.OAuthFlows
import io.swagger.v3.oas.annotations.security.OAuthScope
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server

/**
 * Main application object
 * Started by Docker container.
 */
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
 * @see "https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html"
 */
@OpenAPIDefinition(
    info = Info(
        title = "Micronaut Baseline",
        version = "1.0",
        description = "Micronaut Baseline project used as template for new microservices.",
        contact = Contact(url = "https://vy.no", name = "Team Ruteplan", email = "team-ruteplan@vy.no"),
    ),
    servers = [
        Server(url = "https://services.trafficinfo.vydev.io/baseline-micronaut", description = "Production"),
        Server(url = "https://services.stage.trafficinfo.vydev.io/baseline-micronaut", description = "Stage"),
        Server(url = "https://services.test.trafficinfo.vydev.io/baseline-micronaut", description = "Test"),
        Server(url = "https://services.dev.trafficinfo.vydev.io/baseline-micronaut", description = "Dev"),
    ],
    externalDocs = ExternalDocumentation(
        description = "Internal Application Documentation",
        url = "https://vygruppen.atlassian.net/wiki/spaces/TRAFFICINFO/pages/3793586330/Developer",
    ),
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
                ExtensionProperty(name = "x-amazon-apigateway-authtype", value = "cognito_user_pools"),
            ],
        ),
        Extension(
            name = "x-amazon-apigateway-authorizer",
            properties = [
                ExtensionProperty(name = "providerARNs", value = "[\"\${provider_arn}\"]", parseValue = true),
                ExtensionProperty(name = "type", value = "cognito_user_pools"),
            ],
        ),
    ],
)
// Scheme for documentation
@SecurityScheme(
    name = "security_auth",
    type = SecuritySchemeType.OAUTH2,
    flows = OAuthFlows(
        clientCredentials = OAuthFlow(
            tokenUrl =
            "https://auth.cognito.vydev.io/oauth2/token" +
                "\nhttps://auth.stage.cognito.vydev.io/oauth2/token" +
                "\nhttps://auth.test.cognito.vydev.io/oauth2/token" +
                "\nhttps://auth.dev.cognito.vydev.io/oauth2/token",
            scopes = [
                OAuthScope(name = "https://services.trafficinfo.vydev.io/baseline/read", description = "read scope"),
            ],
        ),
    ),
)
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("no.vy.trafficinfo.baseline.micronaut")
            .mainClass(Application.javaClass)
            .eagerInitSingletons(true)
            .banner(false)
            .start()
    }
}