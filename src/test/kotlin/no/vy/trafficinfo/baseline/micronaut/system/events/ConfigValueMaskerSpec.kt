package no.vy.trafficinfo.baseline.micronaut.system.events

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.PropertySource
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

/**
 * Verifies that configuration logging only shows allowlisted values.
 */
class ConfigValueMaskerSpec :
    BehaviorSpec({

        given("the config value masker") {
            `when`("a key is allowlisted") {
                then("its value is shown") {
                    ConfigValueMasker.mask("jwksUrl", "https://example.com/jwks.json") shouldBe "https://example.com/jwks.json"
                    ConfigValueMasker.mask("micronaut.http.services.whoami.url", "https://x") shouldBe "https://x"
                    ConfigValueMasker.mask("micronaut.security.token.jwt.signatures.jwks.awscognito.url", "https://x") shouldBe
                        "https://x"
                    ConfigValueMasker.mask("jackson.serialization-features.indentOutput", true) shouldBe true
                }

                then("matching is case-insensitive and ignores list indexes") {
                    ConfigValueMasker.isAllowed("JWKSURL") shouldBe true
                    ConfigValueMasker.isAllowed("micronaut.server.cors.configurations.web.allowed-origins[0]") shouldBe true
                }

                then("credentials in urls are removed") {
                    ConfigValueMasker.mask("baseUrl", "postgres://user:supersecret@db:5432/x") shouldBe
                        "postgres://<hidden>@db:5432/x"
                    ConfigValueMasker.mask("micronaut.server.cors.configurations.web.allowed-origins", listOf("https://u:p@h")) shouldBe
                        listOf("https://<hidden>@h")
                }
            }

            `when`("a key is not allowlisted") {
                then("its value is hidden") {
                    listOf(
                        "DB_PASS",
                        "DATABASE_URL",
                        "PRIVATE_PEM",
                        "MY_API_AUTH",
                        "cognito.clientSecret",
                        "cognito.clientId",
                        "elasticcloud.password",
                        "micronaut.security.token.jwt.signatures.secret.generator.secret",
                        "some.new.key",
                    ).forEach { ConfigValueMasker.mask(it, "supersecret") shouldBe ConfigValueMasker.HIDDEN }
                }

                then("a single-segment wildcard does not match deeper keys") {
                    ConfigValueMasker.isAllowed("micronaut.http.services.whoami.ssl.key-store.password") shouldBe false
                }

                then("metrics exporter credentials are hidden, only the listed metrics keys are shown") {
                    ConfigValueMasker.mask("micronaut.metrics.export.datadog.api-key", "supersecret") shouldBe ConfigValueMasker.HIDDEN
                    ConfigValueMasker.mask("micronaut.metrics.export.cloudwatch.namespace", "ns") shouldBe "ns"
                }
            }

            `when`("a resolved value comes from placeholders") {
                val raw =
                    mapOf(
                        "baseUrl" to "\${APM_SECRET_TOKEN}",
                        "micronaut.http.services.a.url" to "\${jwksUrl}/x",
                        "jwksUrl" to "https://example.com",
                        "micronaut.http.services.b.url" to "\${jwksUrl}",
                        "micronaut.http.services.c.url" to "\${CONTEXT_PATH:\${some.secret}}",
                    )

                then("it is hidden if any referenced key is not allowlisted") {
                    ConfigValueMasker.maskResolved("baseUrl", "supersecret", raw::get) shouldBe ConfigValueMasker.HIDDEN
                    ConfigValueMasker.maskResolved("micronaut.http.services.c.url", "supersecret", raw::get) shouldBe
                        ConfigValueMasker.HIDDEN
                }

                then("it is shown if all referenced keys are allowlisted") {
                    ConfigValueMasker.maskResolved("micronaut.http.services.a.url", "https://example.com/x", raw::get) shouldBe
                        "https://example.com/x"
                }
            }
        }

        given("the application config logger") {
            // only the environment is started, not the application context and its beans
            val environment =
                ApplicationContext
                    .builder()
                    .environments("masktest")
                    .propertySources(
                        PropertySource.of(
                            "planted",
                            mapOf(
                                "cognito.clientSecret" to "supersecret1",
                                "datasource.url" to "postgres://user:supersecret2@db/x",
                                "baseUrl" to "https://user:supersecret3@services.example.com",
                                // allowlisted key resolving a secret through a placeholder
                                "planted.secret" to "supersecret6",
                                "micronaut.http.services.alias.url" to "\${planted.secret}",
                                // allowlisted key resolving an allowlisted key
                                "micronaut.http.services.chained.url" to "\${jwksUrl}",
                                // allowlisted key that cannot be resolved, with credentials in the raw value
                                "micronaut.http.services.broken.url" to "https://u:supersecret7@\${missing.key}",
                            ),
                        ),
                    ).build()
                    .environment
            environment.start()
            val configLogger = ApplicationConfigLogger(environment)

            `when`("reporting a property source") {
                val report = configLogger.propertySourceReport(environment.propertySources.first { it.name == "planted" })

                then("secret values are not logged, but keys are") {
                    report shouldNotContain "supersecret"
                    report shouldContain "cognito.clientSecret = <hidden>"
                }
            }

            `when`("reporting the OS environment and system properties") {
                val reports =
                    listOf(
                        configLogger.propertySourceReport(PropertySource.of("env", mapOf("DB_PASS" to "supersecret4"))),
                        configLogger.propertySourceReport(PropertySource.of("system", mapOf("db.pass" to "supersecret5"))),
                    )

                then("no keys or values are logged, only the number of keys") {
                    reports.forEach {
                        it shouldNotContain "supersecret"
                        it shouldNotContain "DB_PASS"
                        it shouldContain "1 keys (values not logged)"
                    }
                }
            }

            `when`("reporting the effective configuration") {
                val report = configLogger.effectiveConfigurationReport()

                then("secret values are not logged") {
                    report shouldNotContain "supersecret"
                }

                then("allowlisted values are logged, resolved") {
                    report shouldContain "baseUrl = https://<hidden>@services.example.com"
                    report shouldContain "micronaut.security.token.jwt.signatures.jwks.awscognito.url = https://cognito-idp."
                    report shouldContain "micronaut.http.services.chained.url = https://cognito-idp."
                }

                then("an allowlisted key resolving a secret through a placeholder is hidden") {
                    report shouldContain "micronaut.http.services.alias.url = <hidden>"
                }

                then("unresolvable values never include the error message") {
                    report shouldContain "micronaut.http.services.broken.url = <unresolvable>\n"
                }
            }

            afterSpec { environment.stop() }
        }
    })