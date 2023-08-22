package de.cramer.bstonotifier.services.notifications

import de.cramer.bstonotifier.utils.Message

interface NotificationService {

    fun notify(message: Message)
}
