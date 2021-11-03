package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.vy.trafficinfo.baseline.micronaut.domain.Health
import no.vy.trafficinfo.baseline.micronaut.services.CallbackClient
import no.vy.trafficinfo.baseline.micronaut.services.WhoamiClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject

/**
 * A secured resource that you need an access token to call.
 *
 * This endpoint is just an example of how to do authorization and authentication
 * and in addition also fine grained access control using custom scopes from
 * Cognito.
 *
 * To call any of these resources you need to set a Authorization: Bearer <token>
 * http header on the request. The token is retrieved from Cognito like described
 * in confluence https://jico.nsb.no/confluence/display/TRAFFICINFO/Authentication+and+Authorization
 */
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/secured")
class SecuredController {

    private val log: Logger = LoggerFactory.getLogger(SecuredController::class.java)

    @Inject
    lateinit var whoamiClient: WhoamiClient

    @Inject
    lateinit var callbackClient: CallbackClient

    /**
     * Secured health endpoint.
     * Everyone that is authenticated should be able to call this endpoint.
     */
    @Get("/health")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("https://services.trafficinfo.vydev.io/baseline-micronaut/read")
    fun securedHealth(): HttpResponse<Health> {
        return HttpResponse.ok(Health())
    }

    /**
     * To test A2A communication to Whoami with authentication.
     * Should propagate the incoming access token by default.
     */
    @Operation(
        summary = "To test A2A communication to Whoami with authentication. " +
            "Should propagate the incoming access token by default.",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(
                    Content(
                        mediaType = "application/json"
                    )
                ),
                description = "A successful request which return a list of Nominal Dates."
            ), ApiResponse(
                responseCode = "400",
                content = arrayOf(
                    Content(
                        mediaType = "application/problem+json"
                    )
                ),
                description = "A successful request which return a list of Nominal Dates."

            )
        ],
        extensions = [
            Extension(
                name = "x-amazon-apigateway-integration",
                properties = [
                    ExtensionProperty(name = "passthroughBehavior", value = "when_no_match"),
                    ExtensionProperty(name = "uri", value = "https://svclb.\${hosted_zone_name}/\${basePath}/secured/whoami"),
                    ExtensionProperty(name = "httpMethod", value = "GET"),
                    ExtensionProperty(name = "type", value = "http_proxy")
                ]
            ), Extension(
                name = "x-amazon-apigateway-request-validator",

                properties = [
                    ExtensionProperty(
                        name = "x-amazon-apigateway-request-validator",
                        value = "Validate body, query string parameters, and headers"
                    )
                ]
            )
        ],
        security = [
            SecurityRequirement(
                name = "cognito_auth",
                scopes = ["https://services.\${hosted_zone_name}/\${basePath}/read"]
            )
        ]
    )
    @Get("/whoami")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("https://services.trafficinfo.vydev.io/baseline-micronaut/read")
    fun securedWhoami(): Mono<String> {
        return whoamiClient.whoami()
    }

    /**
     * To test A2A communication to micronaut-baseline with authentication.
     * Call the baseline service, eg. our self to see what is received
     * in the other end. Should propagate the incoming access token by default.
     */
    @Get("/self")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("https://services.trafficinfo.vydev.io/baseline-micronaut/read")
    fun securedSelf(): Mono<String> {
        return callbackClient.callback("Hello from secured self.")
    }

    /**
     * To test A2A communication to micronaut with authentication.
     * Should propagate the incoming access token by default.
     * @param text is the body posted to the endpoint.
     * @param authentication holds the current authenticated user information.
     */
    @Post("/callback")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("https://services.trafficinfo.vydev.io/baseline-micronaut/read")
    fun securedCallback(
        @Body text: String,
        authentication: Authentication?
    ): HttpResponse<String> {
        val username = if (authentication != null) {
            authentication.name
        } else {
            "anonymous"
        }
        return HttpResponse.ok("got callback from $username")
    }
}