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
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import co.elastic.apm.api.ElasticApm
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import mu.KotlinLogging
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

private val logger = KotlinLogging.logger {}

/**
 * # Declarative Http Client for the Whoami Service.
 */
@Client("whoami")
interface WhoamiClient {
    @Get(value = "/")
    fun get(): HttpResponse<String>
}

@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
@Tags(Tag(name = "Whoami"))
class WhoamiControlle(
    @Client whoamiClient: WhoamiClient,
) {

    @Operation(
        summary = "Whoami request",
        description = "Whoami request",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Success",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "Return value",
                                value = "Hostname: ip-10-100-68-26.eu-west-1.compute.internal\n" +
                                    "IP: 127.0.0.1\n" +
                                    "IP: 169.254.172.42\n" +
                                    "IP: 10.100.68.26\n" +
                                    "RemoteAddr: 10.100.35.87:22454\n" +
                                    "GET /whoami/ HTTP/1.1\n" +
                                    "Host: svclb.dev.trafficinfo.vydev.io\n" +
                                    "User-Agent: Java-http-client/21.0.1\n" +
                                    "X-Amzn-Trace-Id: Root=1-65cc7144-18916edc6beeac7941b66faa\n" +
                                    "X-Forwarded-For: 54.73.204.67\n" +
                                    "X-Forwarded-Port: 443\n" +
                                    "X-Forwarded-Proto: https",
                            ),
                        ],
                    ),
                ],
            ),
        ],
        extensions = arrayOf(
            Extension(
                name = "x-amazon-apigateway-integration",
                properties = [
                    ExtensionProperty(name = "passthroughBehavior", value = "when_no_match"),
                    ExtensionProperty(
                        name = "uri",
                        value = "https://slb.\${hosted_zone_name}/\${base_path}/whoami",
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
    @Get("/whoami")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(): HttpResponse<String> {
        logger.info { "Calling whoami debug service." }
        ElasticApm.currentSpan().setDestinationService("whoami")

        val request = HttpRequest.newBuilder()
            .uri(URI("https://svclb.dev.trafficinfo.vydev.io/whoami/"))
            .GET()
            .build()

        val client = HttpClient.newHttpClient()
        val response = client.send(request, BodyHandlers.ofString())
        logger.info { "Got response response status: ${response.statusCode()}" }
        logger.info { "Echo response response to client." }
        return HttpResponse.ok(response.body())
    }
}