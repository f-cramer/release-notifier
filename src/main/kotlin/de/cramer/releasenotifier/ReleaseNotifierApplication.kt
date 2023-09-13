package de.cramer.releasenotifier

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
class ReleaseNotifierApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<ReleaseNotifierApplication>(*args)
}
