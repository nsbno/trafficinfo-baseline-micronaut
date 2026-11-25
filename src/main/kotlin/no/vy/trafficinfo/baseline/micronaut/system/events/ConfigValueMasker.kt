package no.vy.trafficinfo.baseline.micronaut.system.events

/**
 * Decides which configuration values may be written to the log.
 *
 * This is an ALLOWLIST: only values of keys matching [ALLOWED_KEYS] are logged,
 * every other value is replaced with [HIDDEN]. The key itself is always shown,
 * so the log still tells which keys are set. Forgetting to allowlist a key
 * therefore hides a value, it never leaks a secret.
 *
 * Even allowlisted values have credentials in URLs (`scheme://user:password@host`)
 * removed, in case someone puts them in an otherwise harmless URL key.
 *
 * ## Adding keys
 * Add a pattern to [ALLOWED_KEYS] only if the value is NEVER secret in any
 * environment. Do not add keys that can contain credentials, such as connection
 * strings, client ids/secrets, key stores or anything under an `ssl` section.
 * Prefer exact keys over wildcards.
 *
 * Pattern syntax (matched case-insensitively against the whole key):
 * - `*` matches exactly one key segment (no dots), e.g. `micronaut.http.services.*.url`
 * - `**` matches any number of segments, e.g. `jackson.**`
 * - list entries are matched without their index, e.g. `a.b[0]` is matched as `a.b`
 */
object ConfigValueMasker {
    const val HIDDEN = "<hidden>"

    /**
     * Keys whose values may be logged. See the class documentation before adding to this list.
     */
    val ALLOWED_KEYS: List<String> =
        listOf(
            // service-owned keys
            "baseUrl",
            "jwksUrl",
            "trafficinfoJwksUrl",
            "centralJwksUrl",
            // application and server
            "micronaut.application.name",
            "micronaut.server.port",
            "micronaut.server.context-path",
            "micronaut.server.cors.**",
            "micronaut.router.static-resources.**",
            "micronaut.openapi.**",
            "micronaut.caches.**",
            // outgoing http
            "micronaut.http.client.read-timeout",
            "micronaut.http.client.max-content-length",
            "micronaut.http.services.*.url",
            "micronaut.http.services.*.path",
            "micronaut.http.services.*.health-check",
            "micronaut.http.services.*.health-check-interval",
            "micronaut.http.services.*.health-check-uri",
            // security (no secrets, only switches and public urls)
            "micronaut.security.enabled",
            "micronaut.security.basic-auth.enabled",
            "micronaut.security.endpoints.*.enabled",
            "micronaut.security.token.roles-name",
            "micronaut.security.token.roles-separator",
            "micronaut.security.token.jwt.signatures.jwks.*.url",
            "micronaut.security.token.propagation.enabled",
            "micronaut.security.token.propagation.service-id-regex",
            // metrics, management, json, messaging
            "micronaut.metrics.**",
            "endpoints.**",
            "jackson.**",
            "micronaut.jms.sqs.enabled",
            // aws (region and local endpoints only, never credentials)
            "aws.region",
            "aws.services.*.endpoint-override",
            "aws.services.*.port-override",
        )

    private val allowedPatterns: List<Regex> = ALLOWED_KEYS.map(::toRegex)

    private val urlCredentials = Regex("(?<=://)[^/@\\s]+@")

    private val listIndex = Regex("\\[\\d+]")

    /** True if the value of [key] may be logged. */
    fun isAllowed(key: String): Boolean {
        val normalized = key.replace(listIndex, "")
        return allowedPatterns.any { it.matches(normalized) }
    }

    /** The value to log for [key]: the value itself for allowlisted keys, [HIDDEN] otherwise. */
    fun mask(
        key: String,
        value: Any?,
    ): Any? = if (isAllowed(key)) removeUrlCredentials(value) else HIDDEN

    private fun removeUrlCredentials(value: Any?): Any? =
        when (value) {
            is CharSequence -> value.toString().replace(urlCredentials, "$HIDDEN@")
            is List<*> -> value.map(::removeUrlCredentials)
            is Map<*, *> -> value.mapValues { removeUrlCredentials(it.value) }
            else -> value
        }

    private fun toRegex(pattern: String): Regex {
        val regex =
            pattern
                .split(".")
                .joinToString("\\.") { segment ->
                    when (segment) {
                        "**" -> ".+"
                        "*" -> "[^.]+"
                        else -> Regex.escape(segment)
                    }
                }
        return Regex(regex, RegexOption.IGNORE_CASE)
    }
}