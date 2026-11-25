package io.kotest.provided

import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension
import io.kotest.core.config.AbstractProjectConfig

/**
 * Kotest 6 only picks up project config from this package/class name.
 */
@Suppress("unused")
object ProjectConfig : AbstractProjectConfig() {
    override val extensions = listOf(MicronautKotest5Extension)
}