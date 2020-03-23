package no.vy.trafficinfo.baseline.micronaut.services

import io.reactivex.Single

interface WhoamiOperations {
    fun whoami(): Single<String>
}