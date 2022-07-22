package no.vy.trafficinfo.baseline.micronaut.jobs

import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEventRepositoryImpl
import no.vy.trafficinfo.baseline.micronaut.domain.ChangeEventRepository
import spock.lang.Specification
import spock.lang.Subject

/**
 * # Unit test for ChangeEventCreateJob.
 *
 * Mock out the repository to check that the class calls
 * the repo create method to create new events.
 */
@MicronautTest
class ChangeEventCreateJobSpec extends Specification {

    @MockBean(ChangeEventRepositoryImpl)
    ChangeEventRepository mockRepo() {
        return Mock(ChangeEventRepository)
    }

    @Inject
    ChangeEventRepository repo

    @Subject
    @Inject
    ChangeEventCreateJob job


    /**
     * Mock the repository and make sure that the scheduled job
     * call the repo when job method `createEvent` is called.
     */
    def "should create event"() {

        @Subject
        def job = new ChangeEventCreateJob(repo)

        when:
        job.createEvent()

        then:
        1 * repo.create()
    }

}
