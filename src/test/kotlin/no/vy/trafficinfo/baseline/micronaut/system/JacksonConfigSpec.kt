package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import jakarta.inject.Inject
import java.time.OffsetDateTime

data class JacksonConfigDto(
    val name: String,
    val time: OffsetDateTime,
)

/**
 * Verifies that the `jackson.*-features` keys in application.yml are applied.
 * Micronaut silently ignores unknown keys, so a renamed key would otherwise go unnoticed.
 */
@MicronautTest(startApplication = false)
class JacksonConfigSpec(
    @Inject val mapper: ObjectMapper,
) : BehaviorSpec({

        given("the application object mapper") {
            `when`("deserializing json with an unknown property and an offset date") {
                val dto =
                    mapper.readValue<JacksonConfigDto>(
                        """{"name":"a","time":"2026-01-01T12:00:00+01:00","unknown":true}""",
                    )

                then("the unknown property is ignored (failOnUnknownProperties: false)") {
                    dto.name shouldBe "a"
                }

                then("the offset is kept (adjustDatesToContextTimeZone: false)") {
                    dto.time.offset.totalSeconds shouldBe 3600
                }
            }

            `when`("serializing a date") {
                val json =
                    mapper.writeValueAsString(
                        JacksonConfigDto("a", OffsetDateTime.parse("2026-01-01T12:00:00+01:00")),
                    )

                then("it is written as an ISO string (writeDatesAsTimestamps: false)") {
                    json shouldContain "\"2026-01-01T12:00:00+01:00\""
                }

                then("the output is indented (indentOutput: true)") {
                    json shouldBe
                        """
                        {
                          "name" : "a",
                          "time" : "2026-01-01T12:00:00+01:00"
                        }
                        """.trimIndent()
                }
            }
        }
    })