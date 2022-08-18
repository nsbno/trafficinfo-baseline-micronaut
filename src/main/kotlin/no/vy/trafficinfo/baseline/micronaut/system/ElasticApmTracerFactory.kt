package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes
import jakarta.inject.Singleton

@Factory
class ElasticApmTracerFactory {
    @Singleton
    @Primary
    fun elasticApmTracer(): Tracer {
        val resource: Resource = Resource.getDefault()
            .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "logical-service-name")))

        val sdkTracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build())
            .setResource(resource)
            .build()

        val sdkMeterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
            .setResource(resource)
            .build()

        val openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setMeterProvider(sdkMeterProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal()

        return openTelemetry.getTracer("instrumentation-library-name", "1.0.0"); s
    }
}
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