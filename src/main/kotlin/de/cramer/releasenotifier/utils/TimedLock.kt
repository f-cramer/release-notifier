package de.cramer.releasenotifier.utils

import org.slf4j.Logger
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TimedLock(
    private val minimumDurationBetweenLockings: Duration,
    private val log: Logger,
) {
    private val lock = ReentrantLock()
    private var earliestNextLock: Instant? = null

    fun <T> withLock(action: () -> T): T = lock.withLock {
        val earliestNextRequest = this.earliestNextLock
        val now = Instant.now()
        if (earliestNextRequest != null) {
            if (earliestNextRequest.isAfter(now)) {
                val millis = now.until(earliestNextRequest, ChronoUnit.MILLIS)
                log.trace("sleeping for {} ms", millis)
                Thread.sleep(millis)
            }
        }
        this.earliestNextLock = now + minimumDurationBetweenLockings

        action()
    }
}
