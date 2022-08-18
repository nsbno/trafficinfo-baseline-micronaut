package no.vy.trafficinfo.baseline.micronaut.system
/*

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import co.elastic.apm.opentracing.ElasticApmTracer
import io.opentracing.Tracer
import jakarta.inject.Singleton

@Factory
class ElasticApmTracerFactory {
    @Singleton
    @Primary
    fun elasticApmTracer(): Tracer {
        return object : ElasticApmTracer() {
            override fun buildSpan(operationName: String?): Tracer.SpanBuilder {
                val spanBuilder: Tracer.SpanBuilder = super.buildSpan(operationName)
                val httpUrl = ServerRequestContext.currentRequest<Any?>()
                    .map { request: HttpRequest<Any?> -> request.uri.toString() }
                    .orElse("http://unknown.host/couldNotFindRequestContext")
                return spanBuilder.withTag("http.url", httpUrl)
            }
        }
    }
}*/