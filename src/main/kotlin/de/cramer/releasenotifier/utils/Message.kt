package de.cramer.releasenotifier.utils

import java.time.Instant

data class Message(
    val subject: String,
    val message: String,
    val html: Boolean,
    val timestamp: Instant = Instant.now(),
)
