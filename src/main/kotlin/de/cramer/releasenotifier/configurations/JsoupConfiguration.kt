package de.cramer.releasenotifier.configurations

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.time.Duration

@Suppress("ConfigurationProperties")
@ConfigurationProperties
data class JsoupConfiguration(
    private val jsoup: MutableMap<String, JsoupConnectionProperties> = mutableMapOf(),
) {
    val defaultProperties: JsoupConnectionProperties
        get() = jsoup.computeIfAbsent("default") {
            JsoupConnectionProperties(Duration.ZERO)
        }

    fun getProperties(key: String) = jsoup.getOrElse(key) { defaultProperties }
}

data class JsoupConnectionProperties @ConstructorBinding constructor(
    // no default value because of bug in Spring Boot
    val delayBetweenRequests: Duration,
)
