package no.vy.trafficinfo.baseline.micronaut.services

import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.reactivex.Flowable
import javax.inject.Singleton
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class UserAuthenticationService : AuthenticationProvider {
    private val log: Logger = LoggerFactory.getLogger(UserAuthenticationService::class.java)

    override fun authenticate(authenticationRequest: AuthenticationRequest<*, *>?): Publisher<AuthenticationResponse> {
        log.info("authentication request from user: ${authenticationRequest?.identity}")

        if (authenticationRequest?.identity == "user" && authenticationRequest.secret == "password") {
            return Flowable.just(UserDetails("user", listOf("ROLE_ADMIN")))
        }
        return Flowable.just(AuthenticationFailed())
    }
}