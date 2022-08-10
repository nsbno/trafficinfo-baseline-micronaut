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

import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static spock.util.matcher.HamcrestSupport.that

/**
 * Unit test for Health Check.
 *
 * Call the health check endpoint to check that it is
 * enabled and return status 200.
 *
 */
@MicronautTest
class HealthControllerSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient client

    def "should respond 200 ok on health check"() {
        when: "calling the health check endpoint"
        def result = client.toBlocking().exchange("/health")

        then: "we should get successful http status in return"
        that(result.status(), equalTo(HttpStatus.OK))
    }
}
