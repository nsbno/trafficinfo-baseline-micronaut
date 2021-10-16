package no.vy.trafficinfo.baseline.micronaut.routes

import io.micronaut.scheduling.annotation.Scheduled
import org.apache.camel.Endpoint
import org.apache.camel.builder.ProxyBuilder
import org.slf4j.LoggerFactory
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Just a fake service that creates messages to route direct:start in Camel.
 * It spams Camel with 100 messages per second to see how it handles it.
 * The message it sends is a hard coded ReasonCodes message from TIOS.
 */
@Singleton
class DummyService {

    @Inject
    lateinit var myRouteBuilder: DummyRouteBuilder

    companion object {
        private val LOG = LoggerFactory.getLogger(DummyService::class.java)
    }

    /**
     *
     */
    @Scheduled(fixedDelay = "10ms", initialDelay = "1s")
    fun executeEveryTen() {
        if (myRouteBuilder.context.isStarted) {
            val endpoint: Endpoint = myRouteBuilder.context.getEndpoint("direct:start")
            val service = ProxyBuilder(myRouteBuilder.context).endpoint(endpoint).build(DummyService.MyInterface::class.java)
            val message = createReasonCodesMessage("311", SimpleDateFormat("dd/M/yyyy").format(Date()))
            service.sendMessage(message)
        }
    }

    private interface MyInterface {
        fun sendMessage(s: String)
    }

    fun createReasonCodesMessage(trainId: String, nominalDate: String) = """
        <?xml version="1.0" encoding="UTF-8"?>
        <REASON operation="INSERT">
           <TrainNo></TrainNo>
           <OriginTime>2021.10.15</OriginTime>
           <Cancelled />
           <CancelledCodeTrain>P</CancelledCodeTrain>
           <CancelledCodeStation>D</CancelledCodeStation>
           <Station>GAR</Station>
           <DelayInMinutes>0</DelayInMinutes>
           <DelayRelativ>0</DelayRelativ>
           <EventTime />
           <RegistrationTime>2021.10.15 10:02:16</RegistrationTime>
           <ReasonId>5</ReasonId>
           <ReasonText>Planlagt vedlikeholdsarbeid infrastruktur</ReasonText>
           <ReasonDescription />
        </REASON>        
    """.trimIndent()
}