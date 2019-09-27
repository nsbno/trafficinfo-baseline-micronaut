package no.vy.trafficinfo.baseline.micronaut

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("no.vy.trafficinfo.baseline.micronaut")
                .mainClass(Application.javaClass)
                .start()
    }
}