package de.cramer.releasenotifier.providers.bsto

import de.cramer.releasenotifier.providers.bsto.entities.BsToEpisode
import de.cramer.releasenotifier.providers.bsto.entities.BsToLink
import de.cramer.releasenotifier.providers.bsto.entities.BsToSeason
import de.cramer.releasenotifier.providers.bsto.entities.BsToSeries
import de.cramer.releasenotifier.services.JsoupService
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.http.HttpTimeoutException
import java.time.Duration
import java.util.Locale
import javax.net.ssl.SSLHandshakeException

@Service
class BsToSeriesService(
    private val jsoupService: JsoupService,
) {
    fun update(series: BsToSeries) {
        val languageString = SERIES_URL_REGEX.matchEntire(series.url.toString())?.groupValues?.let { it[2] } ?: SEASON_URL_REGEX.matchEntire(series.url.toString())?.groupValues?.let { it[1] } ?: return
        val language = Locale.forLanguageTag(languageString).toLanguageTag()

        val document = series.url.getDocument() ?: return

        // check for bs.to errors to ignore
        document.selectFirst("body")?.ownText()
            ?.takeIf { IGNORED_ERROR_BODY_REGEX.matches(it) }
            ?.let { return }

        val name = document.selectFirst(NAME_SELECTOR)
            ?.textNodes()?.firstOrNull()?.text()?.trim() ?: error("could not find series name at ($NAME_SELECTOR) in ${series.url}")
        val seasonsElements = document.select(".serie .seasons li a")
            .eachAttr("abs:href")

        series.name = name
        series.language = language
        seasonsElements.forEach { series.addSeason(it) }
    }

    private fun BsToSeries.addSeason(url: String) {
        SEASON_URL_REGEX.matchEntire(url)?.destructured?.let { (_, numberString, languageString) ->
            val number = numberString.toInt()

            val seasonUrl = URI(
                run {
                    val language = Locale.forLanguageTag(languageString).toLanguageTag()
                    if (this.language != language) {
                        url.removeSuffix(languageString) + this.language
                    } else {
                        url
                    }
                },
            )

            val existingSeason = seasons.find { s -> s.number == number }

            val season = if (existingSeason == null) {
                BsToSeason(number, seasonUrl, this)
            } else {
                existingSeason.number = number
                existingSeason
            }
            season.addEpisodes()
            if (existingSeason == null && season.episodes.isNotEmpty()) {
                // add new season only if at least one episode was found
                seasons += season
            }
        }
    }

    private fun BsToSeason.addEpisodes() {
        val latestSeasonDocument = url.getDocument() ?: return

        val selectedLanguage = latestSeasonDocument.selectFirst(".serie .series-language [selected]")?.attr("value") ?: return
        if (selectedLanguage == series.language) {
            val episodeNodes = latestSeasonDocument.select(".episodes tr:not(.disabled)").toList()
            episodeNodes.forEach { addEpisode(it) }
        }
    }

    private fun BsToSeason.addEpisode(element: Element) {
        val firstCell = element.selectFirst("td") ?: return
        val name = firstCell.firstElementChild()?.attr("title")?.takeUnless { it.isBlank() } ?: return
        val number = firstCell.text().takeUnless { it.isBlank() }?.toInt() ?: return

        val existingEpisode = episodes.find { it.number == number }
        val episode = if (existingEpisode == null) {
            BsToEpisode(number, name, this)
        } else {
            existingEpisode.number = number
            existingEpisode.name = name
            existingEpisode
        }

        val linkElements = element.select("td").lastOrNull()?.select("a") ?: return
        linkElements.forEach { episode.addLink(it) }

        if (existingEpisode == null && episode.links.isNotEmpty()) {
            // add new episode only if at least one episode was found
            episodes += episode
        }
    }

    private fun BsToEpisode.addLink(element: Element) {
        val url = URI(element.absUrl("href"))
        val hoster = element.attr("title")

        val existingLink = links.find { it.hoster.equals(hoster, ignoreCase = true) }
        if (existingLink == null) {
            links += BsToLink(0, hoster, url, this)
        } else {
            existingLink.hoster = hoster
            existingLink.url = url
        }
    }

    private fun URI.getDocument(): Document? {
        try {
            return jsoupService.getDocument(this, JSOUP_CONFIGURATION_KEY, timeout = Duration.ofMinutes(1)).document
        } catch (_: HttpTimeoutException) {
            return null
        } catch (_: SocketTimeoutException) {
            return null
        } catch (_: SSLHandshakeException) {
            return null
        } catch (e: SocketException) {
            if (e.message in IGNORED_SOCKET_EXCEPTION_MESSAGES) {
                return null
            }
            throw e
        } catch (e: IOException) {
            if (e.message in IGNORED_IO_EXCEPTION_MESSAGES) {
                return null
            }
            throw e
        }
    }

    companion object {
        private const val JSOUP_CONFIGURATION_KEY = "bsto"

        private const val NAME_SELECTOR = ".serie #sp_left h2"
        private val SERIES_URL_REGEX = """https://bs.to/serie/([^/]+)/([^/]+)/?""".toRegex()
        private val SEASON_URL_REGEX = """https://bs.to/serie/([^/]+)/(\d+)/([^/]+)/?""".toRegex()

        private val IGNORED_ERROR_BODY_REGEX = """Database connection could not be established: SQLSTATE\[[A-Z0-9]+] \[\d+] Too many connections""".toRegex()

        private val IGNORED_SOCKET_EXCEPTION_MESSAGES = setOf(
            "Connection reset",
        )
        private val IGNORED_IO_EXCEPTION_MESSAGES = setOf(
            "Connection reset",
            "Underlying input stream returned zero bytes",
        )
    }
}
