package de.cramer.releasenotifier.services

import de.cramer.releasenotifier.services.notifications.NotificationService
import de.cramer.releasenotifier.utils.Message
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.pre
import kotlinx.html.stream.createHTML
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.Callable

@Service
class CheckConfiguration(
    private val checkerServices: List<CheckerService>,
    private val notificationService: NotificationService,
    @Qualifier("applicationTaskExecutor") private val executor: AsyncTaskExecutor,
    private val log: Logger,
) {

    @Scheduled(cron = "\${check.schedule:-}")
    fun check() {
        checkerServices
            .map {
                executor.submit(
                    Callable {
                        try {
                            it.check()
                        } catch (t: Throwable) {
                            log.error(t.message, t)
                            listOf(t.createMessage(it))
                        }
                    },
                )
            }.asSequence()
            .flatMap { it.get() }
            .forEach { notificationService.notify(it) }
    }

    private fun Throwable.createMessage(checkerService: CheckerService): Message {
        val type = AopProxyUtils.ultimateTargetClass(checkerService).name
        val message = createHTML().html {
            body {
                pre {
                    text(ExceptionUtils.getStackTrace(this@createMessage))
                }
            }
        }
        return Message(
            "Exception while running checker checker service of type $type",
            message,
            true,
        )
    }
}
