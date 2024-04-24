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
import java.util.concurrent.ConcurrentHashMap

@Service
class JsoupService(
    private val log: Logger,
    private val configuration: JsoupConfiguration,
) {
    private val locks = ConcurrentHashMap<String, TimedLock>()

    fun getDocument(
        uri: URI,
        configurationKey: String,
        parser: Parser? = null,
        timeout: Duration? = null,
        ignoreHttpErrors: Boolean? = null,
        lockKey: String = uri.host,
    ): Response = locks.computeIfAbsent(lockKey) {
        TimedLock(it, configuration.getProperties(configurationKey).delayBetweenRequests, log = log).apply {
            log.trace("creating lock for key {}", it)
        }
    }.withLock {
        log.trace("getting document from {}", uri)
        val connection = Jsoup.connect(uri.toString())
        parser?.let { connection.parser(it) }
        timeout?.let { connection.timeout(it.toMillis().toInt()) }
        ignoreHttpErrors?.let { connection.ignoreHttpErrors(it) }

        val response = connection
            .method(Connection.Method.GET)
            .execute()
        val document = response.parse()
        log.trace("done getting and parsing document from {}", uri)
        Response(document, response.statusCode())
    }

    data class Response(
        val document: Document,
        val statusCode: Int,
    )
}
