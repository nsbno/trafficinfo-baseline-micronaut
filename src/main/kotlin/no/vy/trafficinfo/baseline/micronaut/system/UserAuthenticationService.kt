package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.security.authentication.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton

/**
 * This is a user service only used during tests and as an example class.
 * It contains a hard coded test user which you can add roles to.
 *
 * In the cloud env, this class should not be used at all because
 * instead OAuth and tokens are used for user authentication.
 *
 * TODO fix for 2.0 micronaut.
 */
@Singleton
class UserAuthenticationService {
    private val log: Logger = LoggerFactory.getLogger(UserAuthenticationService::class.java)

/*
   override fun authenticate(authenticationRequest: AuthenticationRequest<*, *>?): Publisher<AuthenticationResponse> {
        log.info("authentication request from user: ${authenticationRequest?.identity}")

        if (authenticationRequest?.identity == "user" && authenticationRequest.secret == "password") {
            return Flowable.just(UserDetails("user", listOf("ROLE_ADMIN")))
        }
        return Flowable.just(AuthenticationFailed())
    }
*/
}