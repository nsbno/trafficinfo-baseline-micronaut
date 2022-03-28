package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.core.order.Ordered
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.security.utils.SecurityService
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux

/**
 * Servlet filter to add current user to Logging.
 * TODO move this to common-logging for reuse.
 */
@Filter(Filter.MATCH_ALL_PATTERN)
class UsernameLoggingHttpServerFilter(private val securityService: SecurityService) : HttpServerFilter {

    // make sure the filter is executed last to contain the currently logged-in user.
    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    /**
     * Add username to logging.
     * If no user has been authenticated by security service set the username to anonymous.
     */
    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        return Flux.from(chain.proceed(request)).contextWrite { context -> context.put("user", securityService.username().orElse("anonymous")) }
    }
}