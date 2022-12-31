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
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import jakarta.inject.Inject

/**
 * ## Unit test for TestController.
 *
 * Uses the declarative annotated client to call the Reactor enabled
 * endpoints of TestController to verify that both the streaming flux endpoint
 * and the mono returning just one item works as expected.
 */
@MicronautTest
class SecuredControllerSpec(
    @Client("baseline") val client: SecuredApi,
    @Inject val tokenGenerator: JwtTokenGenerator) : BehaviorSpec({

    fun generateJwt(sub: String, aud: String, scopes: Array<String>): String {
        val claims = mapOf(
            "sub" to sub,
            "aud" to aud,
            "scope" to scopes.joinToString(" ")
        )
        return tokenGenerator.generateToken(claims).get()
    }

    given("given a JwtToken with correct scopes") {
        val jwt = generateJwt(
            "username",
            "requiredAudience",
            arrayOf("https://services.trafficinfo.vydev.io/baseline/read")
        )

        `when`("should be authorized to read-only endpoint if correct read-scope in token") {
            val result = client.get("Bearer $jwt")

            `then`("we should get successful http status in return") {
                result.status.shouldBe(HttpStatus.OK)
            }
        }

        `when`("calling a secured endpoint") {
            val result = client.get("Bearer $jwt")

            `then`("we should get successful http status in return") {
                result.status.shouldBe(HttpStatus.OK)
            }
        }
    }

    given("given a JwtToken with wrong scopes") {
        val jwt = generateJwt(
            "username",
            "requiredAudience",
            arrayOf("https://services.somethingelse.vydev.io/not/correct")
        )

        `when`("calling a secured endpoint") {
            val exception = shouldThrow<HttpClientResponseException> {
                client.get("Bearer $jwt")
            }

            `then`("we should get FORBIDDEN http status in return") {
                exception.status shouldBe HttpStatus.FORBIDDEN
            }
        }
    }
})