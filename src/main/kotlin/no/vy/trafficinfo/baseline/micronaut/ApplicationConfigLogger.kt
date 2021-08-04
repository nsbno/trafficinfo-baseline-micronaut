package no.vy.trafficinfo.baseline.micronaut

import io.micronaut.context.annotation.Context
import io.micronaut.context.env.Environment
import io.micronaut.context.env.PropertySource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Pattern
import javax.annotation.PostConstruct

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
@Context
class ApplicationConfigLogger(val environment: Environment) {
    private val log: Logger = LoggerFactory.getLogger(ApplicationConfigLogger::class.java)

    // mask sensitive information  that should not be printed to the logs.
    private val PROPERTY_NAMES_TO_MASK = arrayOf("password", "credential", "certificate", "key", "secret", "token")
    private val maskPatterns: List<Pattern> = PROPERTY_NAMES_TO_MASK.map { s -> Pattern.compile(".*$s.*", Pattern.CASE_INSENSITIVE) }

    @PostConstruct
    fun logConfig() {
        log.info("Application Configuration - BOF")
        printEnvironments()
        printConfigSources()
        printConfigProperties()
        log.info("Application Configuration - EOF")
    }

    // print the active mironaut environments.
    private fun printEnvironments() = log.info("Environments: ${environment.activeNames}")

    // print the print config properties from all property sources.
    private fun printConfigProperties() = environment.propertySources
        .sortedBy { ps -> ps.order }
        .forEach { ps ->
            log.info("Property Source '${ps.name}'\n${prettyPrintMap(getMaskedProperties(ps), 0)}")
        }

    // print all config sources.
    private fun printConfigSources() = environment.propertySources
        .sortedBy { ps -> ps.order }
        .forEach { ps ->
            log.info("${ps.order} ${ps.name}")
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
    private fun prettyPrintMap(input: Map<String, Any>, level: Int): String = input.entries.map {
        when (it.value) {
            is List<*> -> {
                (it.value as List<Any>).map {
                    prettyPrintMap(it as Map<String, Any>, level + 1)
                }
            }
            is Map<*, *> -> {
                prettyPrintMap(it.value as Map<String, Any>, level + 1)
            }
            else -> {
                indent("${it.key} = ${it.value}", level)
            }
        }
    }.joinToString("\n")

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
                return if (valueString.length > 0) valueString.first() + "*****" else "******"
            }
        }
        return value
    }
}