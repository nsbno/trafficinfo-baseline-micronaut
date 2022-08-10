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

import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEvent
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.isA
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
class ChangeControllerSpec extends Specification {

    @Client("/")
    static interface ChangeClient extends ChangeApi {}

    @Inject
    ChangeClient testClient

    def "should stream as many updates as requested"() {
        when:
        def result = testClient
                .changeEventUpdates()
                .take(3)
                .collectList()
                .block()

        then: "we should gotten 3 items"
        that(result.size(), equalTo(3))
    }

    def "should stream as many records as requested"() {
        when:
        def result = testClient
            .changeEventsAll()
            .take(3)
            .collectList()
            .block()

        then: "we should gotten 3 items"
        that(result.size(), equalTo(3))
    }

    def "should create new change event"() {
        when:
        def result = testClient
                .changeEventCreate()
                .block()

        then: "we should get a ChangeEvent"
        that(result, isA(ChangeEvent))
    }
}