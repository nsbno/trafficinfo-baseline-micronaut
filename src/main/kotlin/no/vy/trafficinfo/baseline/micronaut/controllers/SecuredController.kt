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

import io.micronaut.http.HttpHeaders.AUTHORIZATION
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * # Secured controller
 */
@Controller
@Secured(SecurityRule.IS_AUTHENTICATED)
class SecuredController {

    /**
     * ## Create and return a single change event.
     */
    @Get("/secured")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured("https://services.trafficinfo.vydev.io/baseline/read")
    fun get(@Header(AUTHORIZATION) authorization: String): HttpResponse<Void> {
        logger.info { "User was authenticated and authorized successfully." }
        return HttpResponse.ok()
    }
}