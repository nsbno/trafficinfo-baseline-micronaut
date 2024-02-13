package no.vy.trafficinfo.baseline.micronaut.controllers.cors

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Options
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty

/**
 * Optional to include. Used to enable calls from [stasjon.vydev.io/api-reference](stasjon.vydev.io/api-reference) to test the api.
 *
 * This is a controller that generates an OpenAPI spec for handling CORS preflight requests (see
 * https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request) in AWS API Gateway. It creates a catch-all endpoint for OPTIONS requests, to avoid
 * having to manually configure CORS for each endpoint in API Gateway.
 *
 * This controller is only used for generating the openapi spec. Micronaut is handling the incoming CORS options requests based on the application.yml spec.
 * It is possible to make this controller handle the incoming requests also by using {+proxy+} which denotes both
 * catch-all for micronaut ({+proxy}) and for the API Gateway ({proxy+}) openapi spec
 * https://github.com/micronaut-projects/micronaut-aws/issues/110 -> Micronaut and AWS treats the + sign differently, which is why this is needed
 */
@Controller("/{proxy+}")
@Secured(SecurityRule.IS_ANONYMOUS)
class CorsController {
    @Operation(
        summary = "CORS",
        description = "Enables CORS for all endpoints",
        tags = ["CORS"],
        extensions = [
            Extension(
                name = "x-amazon-apigateway-integration",
                properties = [
                    ExtensionProperty(
                        name = "requestTemplates",
                        value = """{
                            "application/json": "{ \"statusCode\": 200 }"
                        }""",
                        parseValue = true,
                    ),
                    ExtensionProperty(name = "uri", value = "https://slb.\${hosted_zone_name}/\${base_path}/{proxy}"),
                    ExtensionProperty(name = "httpMethod", value = "OPTIONS"),
                    ExtensionProperty(name = "type", value = "http_proxy"),
                    ExtensionProperty(
                        name = "requestParameters",
                        value = """{
                            "integration.request.path.proxy": "method.request.path.proxy"
                        }""",
                        parseValue = true,
                    ),
                ],
            ),
        ],
    )
    @Options
    fun options(): HttpResponse<Map<String, String>> {
        return HttpResponse.ok(emptyMap())
    }
}