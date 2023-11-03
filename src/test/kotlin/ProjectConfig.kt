import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension
import io.kotest.core.config.AbstractProjectConfig

@Suppress("unused")
object ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(MicronautKotest5Extension)
    @Deprecated(
        "Use extensions. This will be removed in 6.0",
        ReplaceWith(
            "listOf(MicronautKotest5Extension)",
            "io.micronaut.test.extensions.kotest5.MicronautKotest5Extension"
        )
    )
    override fun listeners() = listOf(MicronautKotest5Extension)
}