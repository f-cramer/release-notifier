package de.cramer.bstonotifier.services

import de.cramer.bstonotifier.entities.Episode
import de.cramer.bstonotifier.entities.Series
import de.cramer.bstonotifier.repositories.SeriesRepository
import jakarta.mail.internet.InternetAddress
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.h4
import kotlinx.html.html
import kotlinx.html.li
import kotlinx.html.stream.appendHTML
import kotlinx.html.ul
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.StringWriter

@Service
class BsCheckerService(
    private val seriesRepository: SeriesRepository,
    private val seriesService: SeriesService,
    private val mailSender: JavaMailSender,
    private val mailProperties: MailProperties,
    @Value("\${check.recipient.name}") private val recipientName: String,
    @Value("\${check.recipient.address}") private val recipientAddress: String,
) {
    @Transactional
    @Scheduled(cron = "\${check.schedule}")
    fun checkSeries() {
        val allSeries: List<Series> = seriesRepository.findAll()
        val episodesBeforeUpdate = allSeries.allEpisodes

        allSeries.forEach { seriesService.update(it) }
        val episodesAfterUpdate = allSeries.allEpisodes

        val newEpisodes = (episodesAfterUpdate - episodesBeforeUpdate.toSet()).filter { it.links.isNotEmpty() }
        if (newEpisodes.isNotEmpty()) {
            sendNotification(newEpisodes)
        }
    }

    private fun sendNotification(newEpisodes: List<Episode>) {
        val message = createMessage(newEpisodes)
        mailSender.send {
            val helper = MimeMessageHelper(it)
            if (mailProperties.properties["spring.mail.properties.mail.smtp.from"].isNullOrBlank()) {
                helper.setFrom(mailProperties.username)
            }
            helper.setSubject("New episodes")
            helper.setText(message, true)
            helper.setTo(InternetAddress(recipientAddress, recipientName))
        }
    }

    private fun createMessage(newEpisodes: List<Episode>): String {
        val episodesBySeries = newEpisodes.groupBy { it.season.series }.toList()
            .sortedWith(compareBy<Pair<Series, List<Episode>>> { it.first.name }.thenBy { it.first.language })
        val writer = StringWriter()
        writer.appendHTML().html {
            body {
                val seriesText = if (episodesBySeries.size == 1) "series" else "series'"
                h3 { text("New Episodes available for ${episodesBySeries.size} $seriesText") }
                episodesBySeries.forEach { (series, episodes) ->
                    val seasonNumberLength = series.seasons.maxOf { it.number }.toString().length
                    val episodeNumberLength = series.seasons.flatMap { it.episodes }.maxOf { it.number }.toString().length

                    div {
                        h4 { a(href = series.url.toString()) { text("${series.name} (${series.language})") } }
                        ul {
                            episodes.forEach { episode ->
                                li {
                                    text("${episode.season.number.format(seasonNumberLength)}.${episode.number.format(episodeNumberLength)} - ${episode.name}")
                                    ul {
                                        episode.links.forEach { link ->
                                            li {
                                                a(href = link.url.toString()) { text(link.hoster) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return writer.toString()
    }

    private fun Int.format(length: Int): String {
        return String.format("%0${length}d", this)
    }

    private val List<Series>.allEpisodes: List<Episode>
        get() = flatMap { it.seasons }.flatMap { it.episodes }
}
