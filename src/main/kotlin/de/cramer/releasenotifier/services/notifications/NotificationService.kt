package de.cramer.releasenotifier.services.notifications

import de.cramer.releasenotifier.utils.Message

interface NotificationService {

    fun notify(message: Message)
}
