package de.cramer.releasenotifier.services

import de.cramer.releasenotifier.utils.TimedLock
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.time.Duration

@Service
class JsoupService(
    private val log: Logger,
    @Value("\${jsoup.delay-between-requests:0}") private val minimumWaitTimeBetweenRequests: Duration,
) {
    private val locks = mutableMapOf<String, TimedLock>()

    fun getDocument(
        uri: URI,
        parser: Parser? = null,
        timeout: Duration? = null,
        lockKey: String = uri.host,
    ): Document = locks.computeIfAbsent(lockKey) {
        TimedLock(minimumWaitTimeBetweenRequests, log = log)
    }.withLock {
        val connection = Jsoup.connect(uri.toString())
        parser?.let { connection.parser(it) }
        timeout?.let { connection.timeout(it.toMillis().toInt()) }

        connection.get()
    }
}
