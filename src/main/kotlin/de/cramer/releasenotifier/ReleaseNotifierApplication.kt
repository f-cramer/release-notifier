package de.cramer.releasenotifier

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class ReleaseNotifierApplication

fun main(args: Array<String>) {
    runApplication<ReleaseNotifierApplication>(*args)
}
