package de.cramer.bstonotifier.services.notifications

import de.cramer.bstonotifier.utils.Message
import jakarta.mail.internet.InternetAddress
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper

@Configuration
class NotificationServiceConfiguration {

    @Bean
    fun notificationService(
        mailSender: JavaMailSender?,
        mailProperties: MailProperties?,
        @Value("\${check.recipient.name:#null}") recipientName: String?,
        @Value("\${check.recipient.address:#null}") recipientAddress: String?,
    ): NotificationService = if (mailSender == null || mailProperties == null || recipientName == null || recipientAddress == null) {
        LoggingNotificationService()
    } else {
        JavaMailNotificationService(mailSender, mailProperties, recipientName, recipientAddress)
    }

    private class LoggingNotificationService(
        private val log: Logger = LoggerFactory.getLogger(LoggingNotificationService::class.java),
    ) : NotificationService {
        override fun notify(message: Message) = log.debug(message.message)
    }

    private class JavaMailNotificationService(
        private val mailSender: JavaMailSender,
        private val mailProperties: MailProperties,
        private val recipientName: String,
        private val recipientAddress: String,
    ) : NotificationService {
        override fun notify(message: Message) = mailSender.send {
            val helper = MimeMessageHelper(it)
            if (mailProperties.properties["spring.mail.properties.mail.smtp.from"].isNullOrBlank()) {
                helper.setFrom(mailProperties.username)
            }
            helper.setSubject("New episodes")
            helper.setText(message.message, message.html)
            helper.setTo(InternetAddress(recipientAddress, recipientName))
        }
    }
}
