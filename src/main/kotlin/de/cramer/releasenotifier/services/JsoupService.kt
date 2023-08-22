package de.cramer.releasenotifier.services

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class JsoupService(
    private val log: Logger,
    @Value("\${jsoup.delay-between-requests}") private val minimumWaitTimeBetweenRequests: Duration,
) {

    private val lock = ReentrantLock()
    private var earliestNextRequest: Instant? = null

    fun getDocument(uri: URI): Document {
        lock.withLock {
            val earliestNextRequest = this.earliestNextRequest
            val now = Instant.now()
            if (earliestNextRequest != null) {
                if (earliestNextRequest.isAfter(now)) {
                    val millis = now.until(earliestNextRequest, ChronoUnit.MILLIS)
                    log.trace("sleeping for {} ms", millis)
                    Thread.sleep(millis)
                }
            }
            this.earliestNextRequest = now + minimumWaitTimeBetweenRequests

            return Jsoup.connect(uri.toString()).get()
        }
    }
}
