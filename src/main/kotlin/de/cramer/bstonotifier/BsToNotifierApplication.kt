package de.cramer.bstonotifier

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class BsToNotifierApplication

fun main(args: Array<String>) {
    runApplication<BsToNotifierApplication>(*args)
}
