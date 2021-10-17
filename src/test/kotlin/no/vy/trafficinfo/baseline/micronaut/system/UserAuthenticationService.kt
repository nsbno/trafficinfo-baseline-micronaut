package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import jakarta.inject.Singleton

/**
 * This is a Mock Authentication Provider only used during tests and as an example class.
 * It contains a hard coded test user which you can add roles to when doing local development
 * so that you dont need to retrieve tokens to access your service on localhost.
 *
 * In the cloud env, this class should not be used at all because instead OAuth tokens
 * from the Cognito service are used for user authentication.
 */
@Singleton
@Requires(env = [Environment.TEST])
class UserAuthenticationService : AuthenticationProvider {
    private val log: Logger = LoggerFactory.getLogger(UserAuthenticationService::class.java)
    override fun authenticate(httpRequest: HttpRequest<*>?, authenticationRequest: AuthenticationRequest<*, *>?): Publisher<AuthenticationResponse> {
        log.info("authentication request from user: ${authenticationRequest?.identity}")

        if (authenticationRequest?.identity == "user" && authenticationRequest.secret == "password") {
            return Flux.just(AuthenticationResponse.success("user", listOf("https://services.trafficinfo.vydev.io/baseline-micronaut/read")))
        }
        return Flux.just(AuthenticationResponse.failure())
    }
}