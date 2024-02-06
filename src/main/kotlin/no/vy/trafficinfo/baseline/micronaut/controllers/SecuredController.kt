/*
 * Copyright (c)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.vy.trafficinfo.baseline.micronaut.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * # Secured controller
 */
@Controller("/secured")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Tags(Tag(name = "Secured"))
class SecuredController {

    /**
     * ## Create and return a single change event.
     */
    @Operation(
        summary = "Send authorized request",
        description = "Send authorized request",
        extensions = arrayOf(
            Extension(
                name = "x-amazon-apigateway-integration",
                properties = [
                    ExtensionProperty(name = "passthroughBehavior", value = "when_no_match"),
                    ExtensionProperty(
                        name = "uri",
                        value = "https://slb.\${hosted_zone_name}/\${base_path}/secured",
                    ),
                    ExtensionProperty(name = "httpMethod", value = "GET"),
                    ExtensionProperty(name = "type", value = "http_proxy"),
                ],
            ),
            Extension(
                name = "x-amazon-apigateway-request-validator",

                properties = [
                    ExtensionProperty(
                        name = "x-amazon-apigateway-request-validator",
                        value = "Validate body, query string parameters, and headers",
                    ),
                ],
            ),
        ),
    )
    @Get
    @Produces(MediaType.APPLICATION_JSON)
    @Secured("https://services.trafficinfo.vydev.io/baseline/read")
    @SecurityRequirement(
        // For documentation
        name = "security_auth",
        scopes = ["https://services.trafficinfo.vydev.io/baseline/read"],
    )
    fun get(): HttpResponse<Void> {
        logger.info { "User was authenticated and authorized successfully." }
        return HttpResponse.ok()
    }
}