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

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEvent
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEventRepositoryImpl
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.util.concurrent.Queues

private val logger = KotlinLogging.logger {}

/**
 * # Interface for the controller endpoints.
 *
 * Used to generate client to communicate with the
 * controller from the Unit Test.
 */
interface ChangeApi {

    /**
     * ## Return a stream of events.
     *
     * The controller generates one event every second.
     * The annotation @Consumes sets the accept-type
     * on the request so that the client calls the
     * correct endpoint.
     */
    @Get(value = "/changes")
    @Consumes(MediaType.APPLICATION_JSON_STREAM)
    fun changeEventUpdates(): Flux<ChangeEvent>

    /**
     * ## Return all records from repository.
     */
    @Get(value = "/changes")
    @Consumes(MediaType.APPLICATION_JSON)
    fun changeEventsAll(): Flux<ChangeEvent>

    /**
     * ## Create and return a single event.
     */
    @Post(value = "/changes")
    @Consumes(MediaType.APPLICATION_JSON)
    fun changeEventCreate(): Mono<ChangeEvent>
}

/**
 * # Controller that uses Flux and Mono to use Reactor features with Micronaut.
 * The controller expose three endpoints.
 * - ChangeEvent updates as a APPLICATION_JSON_STREAM
 * - all ChangeEvent from repo sa APPLICATION_JSON
 * - insert new ChangeEvent
 *
 */
@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
open class ChangeController(private val repo: ChangeEventRepositoryImpl) : ChangeApi {

    /* The sink where new events are broadcast from.
     * autoCancel is set to false so that the sink is
     * not cancelled when subscriber disconnects.
     * */
    private var sink = Sinks
        .many()
        .multicast()
        .onBackpressureBuffer<ChangeEvent>(
            Queues.SMALL_BUFFER_SIZE, false
        )

    /**
     * Listen for ApplicationEvents where new
     * ChangeEvents has been created.
     */
    @EventListener
    fun onNewChangeEvent(event: ChangeEvent) {
        logger.info { "Received new ChangeEvent: $event" }
        if (sink.currentSubscriberCount() > 0) {
            logger.info { "Publish Event to ${sink.currentSubscriberCount()} subscribers: $event" }
            sink.tryEmitNext(event)
        } else
            logger.info { "Don't publish and events, we dont have any subscribers at the moment." }
    }

    /**
     * ## Stream Change Events from sink.
     *
     * This endpoint will send one event every second as long
     * as there are clients connected.
     */
    @Get("/changes")
    @Produces(MediaType.APPLICATION_JSON_STREAM)
    @Secured(SecurityRule.IS_ANONYMOUS)
    override fun changeEventUpdates(): Flux<ChangeEvent> {
        return sink.asFlux()
    }

    /**
     * ## Create and return a single change event.
     */
    @Get("/changes")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured(SecurityRule.IS_ANONYMOUS)
    override fun changeEventsAll(): Flux<ChangeEvent> {
        return repo.all()
    }

    @Get("/error")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured(SecurityRule.IS_ANONYMOUS)
    fun error(): Flux<ChangeEvent> {
        throw RuntimeException("failed!!")
    }

    /**
     * ## Create and return a single change event.
     *
     * TODO migrate Mono to Coroutine when done.
     */
    @Post("/changes")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured(SecurityRule.IS_ANONYMOUS)
    override fun changeEventCreate() = runBlocking {
        Mono.just(repo.create())
    }
}