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
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static spock.util.matcher.HamcrestSupport.that

/**
 * Unit test for TestController.
 *
 * Uses the declarative annotated client to call
 * the Reactor enabled endpoints of TestController
 * to verify that both the streaming flux endpoint
 * and the mono returning just one item works as
 * expected.
 */
@MicronautTest
class SecuredControllerSpec extends Specification {

    @Client("/")
    static interface SecuredClient extends SecuredApi {}

    @Inject
    SecuredClient testClient

    @Inject
    JwtTokenGenerator tokenGenerator

    def "should be authorized to read-only endpoint if correct read-scope in token"() {
        given: "given a JwtToken with valid user and scope"

        def token = generateJwt(
            "username",
            "requiredAudience",
            ["https://services.trafficinfo.vydev.io/baseline/read"] as String[]
        )
        when: "calling a secured endpoint"
        def result = testClient.get("Bearer $token")

        then: "we should get successful http status in return"
        that(result.status(), equalTo(HttpStatus.OK))
    }

    def "should be denied access to -read-only endpoint if incorrect read-scope in token"() {
        given: "given a JwtToken with valid user and scope"

        def token = generateJwt(
            "username",
            "requiredAudience",
            ["https://services.trafficinfo.vydev.io/seomthing/else"] as String[]
        )
        when: "calling a secured endpoint"
        testClient.get("Bearer $token")

        then: "we should get successful http status in return"
        def e = thrown(HttpClientResponseException)
        that(e.status, equalTo(HttpStatus.FORBIDDEN))
    }


    private String generateJwt(String sub, String aud, String[] scopes) {
        def claims = [
            "sub"  : sub,
            "aud"  : aud,
            "scope": scopes.join(" ")
        ]
        return tokenGenerator.generateToken(claims).get()
    }
}
