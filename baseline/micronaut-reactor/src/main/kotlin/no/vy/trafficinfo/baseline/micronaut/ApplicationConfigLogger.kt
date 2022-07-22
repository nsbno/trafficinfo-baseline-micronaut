package no.vy.trafficinfo.baseline.micronaut

import io.micronaut.context.annotation.Context
import io.micronaut.context.env.Environment
import io.micronaut.context.env.PropertySource
import io.micronaut.core.version.VersionUtils.MICRONAUT_VERSION
import io.micronaut.runtime.context.scope.refresh.RefreshEvent
import io.micronaut.runtime.event.annotation.EventListener
import mu.KotlinLogging
import javax.annotation.PostConstruct
import java.util.regex.Pattern

private val logger = KotlinLogging.logger {}

/**
 * Dump all configuration to log after context has been created.
 *
 * Read interesting information about the configuration sources
 * for the application and print it to the log so that its easy
 * to find out after a container has been started what config
 * was applied.
 *
 * @see logConfig When the Context has been created the logConfig
 * function will be executed.
 */
@Suppress("Unused")
@Context
class ApplicationConfigLogger(private val environment: Environment) {

    // mask sensitive information  that should not be printed to the logs.
    private val PROPERTY_NAMES_TO_MASK = arrayOf("password", "credential", "certificate", "key", "secret", "token")
    private val maskPatterns: List<Pattern> = PROPERTY_NAMES_TO_MASK.map { s -> Pattern.compile(".*$s.*", Pattern.CASE_INSENSITIVE) }

    @EventListener
    fun onRefreshEvent(event: RefreshEvent?) {
        // Listen to the refresh event and print the updated configuration.
        logConfig()
    }

    @PostConstruct
    fun logConfig() {
        logger.info("Application Configuration - BOF")
        printMicronautVersion()
        printEnvironments()
        printConfigSources()
        printConfigProperties()
        logger.info("Application Configuration - EOF")
    }

    // print the active micronaut version.
    private fun printMicronautVersion() = logger.info("Micronaut (v${MICRONAUT_VERSION ?: "???"})")

    // print the active mironaut environments.
    private fun printEnvironments() = logger.info("Environments: ${environment.activeNames}")

    // print the print config properties from all property sources.
    private fun printConfigProperties() = environment.propertySources
        .sortedBy { ps -> ps.order }
        .forEach { ps ->
            logger.info("Property Source '${ps.name}'\n${prettyPrintMap(getMaskedProperties(ps), 0)}")
        }

    // print all config sources.
    private fun printConfigSources() = environment.propertySources
        .sortedBy { ps -> ps.order }
        .forEach { ps ->
            logger.info("${ps.order} ${ps.name}")
        }

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
    private fun prettyPrintMap(input: Map<*, *>, level: Int): String = input.entries.joinToString("\n") {
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
    private fun prettyPrintList(input: List<*>, level: Int): String = input.joinToString("\n") {
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
    private fun indent(input: String, indent: Int): String {
        return input.prependIndent("  ".repeat(indent + 1))
    }

    /**
     * Get all properties from property source and mask all sensitive fields.
     * @param propertySource to read the properties from.
     * @return a key value map of config properties with masked values.
     */
    private fun getMaskedProperties(propertySource: PropertySource): Map<String, Any> {
        val properties = LinkedHashMap<String, Any>()
        propertySource.forEach { k -> properties[k] = maskProperty(k, propertySource.get(k)) }
        return properties
    }

    /**
     * Match for sensitive property keys and mask values.
     */
    private fun maskProperty(key: String, value: Any): Any {
        for (pattern in this.maskPatterns) {
            if (pattern.matcher(key).matches()) {
                val valueString = value.toString()
                return if (valueString.isNotEmpty()) valueString.first() + "*****" else "******"
            }
        }
        return value
    }
}