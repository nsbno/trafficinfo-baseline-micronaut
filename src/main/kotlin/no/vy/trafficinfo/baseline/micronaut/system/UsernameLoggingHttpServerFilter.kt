package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.security.utils.SecurityService
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

/**
 * Servlet filter to add current user to Logging.
 * TODO move this to common-logging for resuse.
 */
@Filter("/**")
class UsernameLoggingHttpServerFilter(val securityService: SecurityService) : HttpServerFilter {
    private val log: Logger = LoggerFactory.getLogger(UsernameLoggingHttpServerFilter::class.java)

    // make sure the filter is executed last to contain the current logged in user.
    override fun getOrder(): Int = Int.MAX_VALUE

    /**
     * Add username to logging.
     * If no user has been authenticated by security service set the username to anonymous.
     */
    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        // get username from security service.
        if (securityService.username().isPresent) {
            MDC.put("user", securityService.username().get())
        } else {
            MDC.put("user", "anonymous")
        }

        return chain.proceed(request)
    }
}