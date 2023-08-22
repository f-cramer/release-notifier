package de.cramer.releasenotifier.services

import de.cramer.releasenotifier.utils.Message

interface CheckerService {
    fun check(): Message?
}
