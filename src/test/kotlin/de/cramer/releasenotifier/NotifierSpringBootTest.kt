package de.cramer.releasenotifier

import org.springframework.boot.test.context.SpringBootTest
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@SpringBootTest(properties = ["spring.flyway.locations = /db/migration,/db/{vendor}/migration"])
annotation class NotifierSpringBootTest
