package no.vy.trafficinfo.baseline.micronaut.system.events

import io.micronaut.context.annotation.Context
import io.micronaut.context.env.Environment
import io.micronaut.context.env.PropertySource
import io.micronaut.context.exceptions.ConfigurationException
import io.micronaut.core.version.VersionUtils
import mu.KotlinLogging
import jakarta.annotation.PostConstruct

private val logger = KotlinLogging.logger {}

/**
 * Dump all configuration to log after context has been created.
 *
 * Read interesting information about the configuration sources
 * for the application and print it to the log so that its easy
 * to find out after a container has been started what config
 * was applied.
 *
 * Values are only logged for keys allowlisted in [ConfigValueMasker];
 * all other values are hidden. The OS environment and system properties
 * are never logged, only how many keys they contain.
 *
 * @see logConfig When the Context has been created the logConfig
 * function will be executed.
 */
@Suppress("Unused")
@Context
class ApplicationConfigLogger(
    private val environment: Environment,
) {
    @PostConstruct
    fun logConfig() {
        logger.info("Application Configuration - BOF")
        printMicronautVersion()
        printEnvironments()
        printConfigSources()
        printConfigProperties()
        logger.info(effectiveConfigurationReport())
        logger.info("Application Configuration - EOF")
    }

    // print the active micronaut version.
    private fun printMicronautVersion() = logger.info("Micronaut (v${VersionUtils.MICRONAUT_VERSION ?: "???"})")

    // print the active micronaut environments.
    private fun printEnvironments() = logger.info("Environments: ${environment.activeNames}")

    // print the print config properties from all property sources.
    private fun printConfigProperties() =
        environment.propertySources
            .sortedBy { ps -> ps.order }
            .forEach { ps -> logger.info(propertySourceReport(ps)) }

    // print all config sources.
    private fun printConfigSources() =
        environment.propertySources
            .sortedBy { ps -> ps.order }
            .forEach { ps ->
                logger.info("${ps.order} ${ps.name}")
            }

    /**
     * Describe one property source. Values are masked with [ConfigValueMasker];
     * for the OS environment and system properties only the number of keys is shown.
     */
    internal fun propertySourceReport(propertySource: PropertySource): String {
        if (propertySource.name in UNLOGGED_SOURCES) {
            return "Property Source '${propertySource.name}': ${propertySource.count()} keys (values not logged)"
        }
        return "Property Source '${propertySource.name}'\n${prettyPrintMap(getMaskedProperties(propertySource), 0)}"
    }

    /**
     * Print the effective (resolved) value of every configured key,
     * so that placeholders like ${jwksUrl} can be verified after startup.
     * Keys only set in the OS environment or system properties are not listed.
     */
    internal fun effectiveConfigurationReport(): String {
        val keys =
            environment.propertySources
                .filter { ps -> ps.name !in UNLOGGED_SOURCES }
                .flatMap { ps -> ps.toList() }
                .toSortedSet()
        val effective =
            keys.joinToString("\n") { key ->
                val value =
                    try {
                        environment
                            .getProperty(key, Any::class.java)
                            .map { ConfigValueMasker.maskResolved(key, it, ::rawValueOf) }
                            .orElse("<unset>")
                    } catch (e: ConfigurationException) {
                        // never log the message: it can contain the raw value, credentials included.
                        // The raw (unresolved) value of allowlisted keys is shown in the property source dump.
                        "<unresolvable>"
                    }
                "  $key = $value"
            }
        return "Effective configuration\n$effective"
    }

    /**
     * The unresolved value of [key] from the property source that wins
     * (highest order; on equal order the one added last).
     */
    private fun rawValueOf(key: String): Any? =
        environment.propertySources
            .sortedBy { ps -> ps.order }
            .asReversed()
            .firstNotNullOfOrNull { ps -> ps.get(key) }

    /**
     * Convert map to String recursively on all values.
     * All pairs in the input map will be converted to String and returned.
     * If the value is itself a List or Map the function will recursively
     * call itself and convert the value again to a String and for each level
     * it will indent the generated String to show the level.
     *
     * @param input is the Map to convert to string.
     * @param level is how deep it has recursively traversed.
     * @return a String generated from the input Map.
     */
    private fun prettyPrintMap(
        input: Map<*, *>,
        level: Int,
    ): String =
        input.entries.joinToString("\n") {
            when (val value = it.value) {
                is List<*> -> {
                    indent("${it.key} = \n" + prettyPrintList(value, level + 1), level)
                }

                is Map<*, *> -> {
                    prettyPrintMap(value, level + 1)
                }

                else -> {
                    indent("${it.key} = ${it.value}", level)
                }
            }
        }

    /**
     * Convert list to String recursively on all values.
     * All items in the input list will be converted to String and returned.
     * If the value is itself a List or Map the function will recursively
     * call the appropriate function and convert the value again to a String
     * and for each level it will indent the generated String to show the level.
     *
     * @param input is the List to convert to string.
     * @param level is how deep it has recursively traversed.
     * @return a String generated from the input Map.
     */
    private fun prettyPrintList(
        input: List<*>,
        level: Int,
    ): String =
        input.joinToString("\n") {
            when (it) {
                is List<*> -> {
                    prettyPrintList(it, level + 1)
                }

                is Map<*, *> -> {
                    prettyPrintMap(it, level + 1)
                }

                else -> {
                    indent("- $it", level)
                }
            }
        }

    /**
     * Ident the input with a given number of spaces.
     *
     * @param indent the number of idents, zero based.
     * @param input is the string to prepend with indent.
     * @return the resulting indented string based on input.
     */
    private fun indent(
        input: String,
        indent: Int,
    ): String = input.prependIndent("  ".repeat(indent + 1))

    /**
     * Get all properties from property source with values masked by [ConfigValueMasker].
     * @param propertySource to read the properties from.
     * @return a key value map of config properties with masked values.
     */
    private fun getMaskedProperties(propertySource: PropertySource): Map<String, Any> {
        val properties = LinkedHashMap<String, Any>()
        propertySource.forEach { k ->
            propertySource.get(k)?.let { properties[k] = ConfigValueMasker.mask(k, it) ?: ConfigValueMasker.HIDDEN }
        }
        return properties
    }

    companion object {
        // property sources holding the OS environment and JVM system properties.
        private val UNLOGGED_SOURCES = setOf("env", "system")
    }
}