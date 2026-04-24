package de.cramer.releasenotifier.services.notifications

import de.cramer.releasenotifier.utils.Message
import jakarta.mail.internet.InternetAddress
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.mail.autoconfigure.MailProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.writeText

@Configuration
class NotificationServiceConfiguration {

    @Bean
    fun notificationService(
        mailSender: JavaMailSender?,
        mailProperties: MailProperties?,
        @Value($$"${check.recipient.name:#{null}}") recipientName: String?,
        @Value($$"${check.recipient.address:#{null}}") recipientAddress: String?,
    ): NotificationService = if (mailSender == null || mailProperties == null || recipientName == null || recipientAddress == null) {
        LoggingNotificationService()
    } else {
        JavaMailNotificationService(mailSender, mailProperties, recipientName, recipientAddress)
    }

    @Bean
    @ConditionalOnProperty(name = ["check.filesystem.directory"])
    fun fileSystemWritingMessageListenerService(
        @Value($$"${check.filesystem.directory}") path: String,
    ): FileSystemWritingMessageListenerService {
        val p = Path.of(path)
        if (!p.isDirectory()) {
            p.createDirectories()
        }

        return FileSystemWritingMessageListenerService(p)
    }

    private class LoggingNotificationService(
        private val log: Logger = LoggerFactory.getLogger(LoggingNotificationService::class.java),
    ) : NotificationService {
        @EventListener
        override fun notify(message: Message) = log.debug(message.message)
    }

    private class JavaMailNotificationService(
        private val mailSender: JavaMailSender,
        private val mailProperties: MailProperties,
        private val recipientName: String,
        private val recipientAddress: String,
    ) : NotificationService {
        @EventListener
        override fun notify(message: Message) = mailSender.send {
            val helper = MimeMessageHelper(it)
            if (mailProperties.properties["spring.mail.properties.mail.smtp.from"].isNullOrBlank()) {
                mailProperties.username?.let(helper::setFrom)
            }
            helper.setSubject(message.subject)
            helper.setText(message.message, message.html)
            helper.setTo(InternetAddress(recipientAddress, recipientName))
        }
    }

    class FileSystemWritingMessageListenerService(
        private val path: Path,
    ) {
        @EventListener
        fun onMessage(message: Message) {
            val p = path.resolve("${message.timestamp} - ${message.subject}")
            p.writeText(message.message, Charsets.UTF_8)
        }
    }
}
