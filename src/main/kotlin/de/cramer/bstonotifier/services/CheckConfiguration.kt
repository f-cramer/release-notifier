package de.cramer.bstonotifier.services

import de.cramer.bstonotifier.services.notifications.NotificationService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CheckConfiguration(
    private val checkerServices: List<CheckerService>,
    private val notificationService: NotificationService,
) {

    @Scheduled(cron = "\${check.schedule}")
    fun check() {
        checkerServices.asSequence()
            .mapNotNull { it.check() }
            .forEach { notificationService.notify(it) }
    }
}
