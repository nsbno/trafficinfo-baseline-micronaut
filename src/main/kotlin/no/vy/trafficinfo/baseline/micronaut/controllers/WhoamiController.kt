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

/**
 * # Secured controller
 */
@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
class WhoamiControlle(
    @Client whoamiClient: WhoamiClient
) {

    /**
     * ## Create and return a single change event.
     */
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