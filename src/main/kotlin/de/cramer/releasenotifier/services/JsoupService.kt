package de.cramer.releasenotifier.services

import de.cramer.releasenotifier.configurations.JsoupConfiguration
import de.cramer.releasenotifier.utils.TimedLock
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.net.URI
import java.time.Duration

@Service
class JsoupService(
    private val log: Logger,
    private val configuration: JsoupConfiguration,
) {
    private val locks = mutableMapOf<String, TimedLock>()

    fun getDocument(
        uri: URI,
        configurationKey: String,
        parser: Parser? = null,
        timeout: Duration? = null,
        ignoreHttpErrors: Boolean? = null,
        lockKey: String = uri.host,
    ): Response = locks.computeIfAbsent(lockKey) {
        TimedLock(configuration.getProperties(configurationKey).delayBetweenRequests, log = log)
    }.withLock {
        val connection = Jsoup.connect(uri.toString())
        parser?.let { connection.parser(it) }
        timeout?.let { connection.timeout(it.toMillis().toInt()) }
        ignoreHttpErrors?.let { connection.ignoreHttpErrors(it) }

        val response = connection
            .method(Connection.Method.GET)
            .execute()
        Response(response.parse(), response.statusCode())
    }

    data class Response(
        val document: Document,
        val statusCode: Int,
    )
}
