package no.vy.trafficinfo.baseline.micronaut.jobs

import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Named
import no.vy.trafficinfo.baseline.micronaut.services.CreateEventService
import no.vy.trafficinfo.baseline.micronaut.services.CreateEventServiceImpl
import org.spockframework.mock.MockUtil
import spock.lang.Specification
import spock.lang.Subject

/**
 * # Unit test for ChangeEventCreateJob.
 *
 * Mock out the repository to check that the class calls
 * the repo create method to create new events.
 */
@MicronautTest(startApplication = false)
class EventCreateJobSpec extends Specification {

    def mockUtil = new MockUtil()

    @MockBean(CreateEventServiceImpl)
    @Named("create_event")
    CreateEventService mockSvc() {
        return Mock(CreateEventService)
    }

    @Inject
    CreateEventService svc

    @Subject
    @Inject
    EventCreateJob job

    def 'check should use mock of CreateEventService'() {
        mockUtil.isMock(svc)
    }


    /**
     * Mock the repository and make sure that the scheduled job
     * call the repo when job method `createEvent` is called.
     */
    def "should create event"() {

        @Subject
        def job = new EventCreateJob(svc)

        when:
        job.createEvent(_ as kotlin.coroutines.Continuation)

        then:
        1 * svc.createEvent(_ as kotlin.coroutines.Continuation)
    }

}
