package de.cramer.bstonotifier.services

import de.cramer.bstonotifier.entities.Episode
import de.cramer.bstonotifier.entities.Link
import de.cramer.bstonotifier.entities.Season
import de.cramer.bstonotifier.entities.Series
import org.jsoup.nodes.Element
import org.springframework.stereotype.Service
import java.net.URI
import java.util.Locale

@Service
class SeriesService(
    private val jsoupService: JsoupService,
) {
    fun update(series: Series) {
        val languageString = SERIES_URL_REGEX.matchEntire(series.url.toString())?.groupValues?.let { it[2] } ?: SEASON_URL_REGEX.matchEntire(series.url.toString())?.groupValues?.let { it[1] } ?: return
        val language = Locale.forLanguageTag(languageString).toLanguageTag()

        val document = jsoupService.getDocument(series.url)
        val name = document.selectFirst(NAME_SELECTOR)
            ?.textNodes()?.firstOrNull()?.text()?.trim() ?: error("could not find series name at ($NAME_SELECTOR)")
        val seasonsElements = document.select(".serie .seasons li a")
            .eachAttr("abs:href")

        series.name = name
        series.language = language
        seasonsElements.forEach { series.addSeason(it) }
    }

    private fun Series.addSeason(url: String) {
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
                val season = Season(number, seasonUrl, this)
                season
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

    private fun Season.addEpisodes() {
        val latestSeasonDocument = jsoupService.getDocument(url)
        val selectedLanguage = latestSeasonDocument.selectFirst(".serie .series-language [selected]")?.attr("value") ?: return
        if (selectedLanguage == series.language) {
            val episodeNodes = latestSeasonDocument.select(".episodes tr:not(.disabled)").toList()
            episodeNodes.forEach { addEpisode(it) }
        }
    }

    private fun Season.addEpisode(element: Element) {
        val firstCell = element.selectFirst("td") ?: return
        val name = firstCell.firstElementChild()?.attr("title")?.takeUnless { it.isBlank() } ?: return
        val number = firstCell.text().takeUnless { it.isBlank() }?.toInt() ?: return

        val existingEpisode = episodes.find { it.number == number }
        val episode = if (existingEpisode == null) {
            val episode = Episode(number, name, this)
            episodes += episode
            episode
        } else {
            existingEpisode.number = number
            existingEpisode.name = name
            existingEpisode
        }

        val linkElements = element.select("td").lastOrNull()?.select("a") ?: return
        linkElements.forEach { episode.addLink(it) }
    }

    private fun Episode.addLink(element: Element) {
        val url = URI(element.absUrl("href"))
        val hoster = element.attr("title")

        val existingLink = links.find { it.hoster == hoster }
        if (existingLink == null) {
            links += Link(0, hoster, url, this)
        } else {
            existingLink.hoster = hoster
            existingLink.url = url
        }
    }

    companion object {
        private const val NAME_SELECTOR = ".serie #sp_left h2"
        private val SERIES_URL_REGEX = """https://bs.to/serie/(.+)/(.+)""".toRegex()
        private val SEASON_URL_REGEX = """https://bs.to/serie/(.+)/(\d+)/(.+)""".toRegex()
    }
}
