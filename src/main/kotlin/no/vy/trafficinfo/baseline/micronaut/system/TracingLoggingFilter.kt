package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.core.order.Ordered
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import org.reactivestreams.Publisher

/**
 * Servlet filter to add tracing header to Logging.
 * TODO move this to common-logging for reuse.
 */
@Filter(Filter.MATCH_ALL_PATTERN)
class TracingLoggingFilter : HttpServerFilter {

    // make sure the filter is executed last to contain the currently logged-in user.
    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    /**
     * Add TRACING uuid to request and return the tracing id in http response.
     */
    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        return chain.proceed(request)
/*
        val transaction = ElasticApm.startTransactionWithRemoteParent { request.getHeaders().get("X-TRACE-ID") }
        try {
            transaction.activate().use { scope ->
                transaction.startSpan()
                transaction.setName(request.uri.path)
                transaction.setType(Transaction.TYPE_REQUEST)
                MDC.put("trace", transaction.traceId)
                return Flux.from(chain.proceed(request)).doOnNext { res ->
                    res.headers.add("X-TRACE-ID", transaction.traceId)
                }.contextWrite {
                    it.put("tracingId", transaction.traceId)
                }
            }
        } catch (e: Exception) {
            transaction.captureException(e)
            throw e
        } finally {
            transaction.end()
        }
*/
    }
}