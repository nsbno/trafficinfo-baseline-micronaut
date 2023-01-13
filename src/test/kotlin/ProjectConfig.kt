import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension
import io.kotest.core.config.AbstractProjectConfig

@Suppress("unused")
object ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(MicronautKotest5Extension)
    override fun listeners() = listOf(MicronautKotest5Extension)
}