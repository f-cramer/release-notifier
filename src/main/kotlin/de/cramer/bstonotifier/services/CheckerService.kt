package de.cramer.bstonotifier.services

import de.cramer.bstonotifier.utils.Message

interface CheckerService {
    fun check(): Message?
}
