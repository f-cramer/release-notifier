package de.cramer.releasenotifier.utils

import org.slf4j.Logger
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TimedLock(
    private val name: String,
    private val minimumDurationBetweenLockings: Duration,
    private val log: Logger,
) {
    private val lock = ReentrantLock()
    private var earliestNextLock: Instant? = null
        set(value) {
            log.trace("setting earliestNextLock to {} for {}", value, name)
            field = value
        }

    fun <T> withLock(action: () -> T): T = lock.withLock {
        val earliestNextRequest = this.earliestNextLock
        var now = Instant.now()
        if (earliestNextRequest != null) {
            while (earliestNextRequest.isAfter(now)) {
                val millis = (now.until(earliestNextRequest, ChronoUnit.NANOS) / TimeUnit.MILLISECONDS.toNanos(1).toDouble()).coerceAtLeast(1.0).toLong()
                log.trace("sleeping for {} ms", millis)
                Thread.sleep(millis)
                now = Instant.now()
            }
        }
        this.earliestNextLock = (now + minimumDurationBetweenLockings).with(ChronoField.NANO_OF_SECOND, 0L)

        log.trace("running action for {}", name)
        action()
    }
}
