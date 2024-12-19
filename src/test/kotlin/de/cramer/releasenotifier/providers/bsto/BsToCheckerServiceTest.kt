package de.cramer.releasenotifier.providers.bsto

import assertk.all
import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.hasSameSizeAs
import assertk.assertions.isEmpty
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isTrue
import assertk.assertions.prop
import assertk.assertions.size
import de.cramer.releasenotifier.NotifierSpringBootTest
import de.cramer.releasenotifier.providers.bsto.entities.BsToEpisode
import de.cramer.releasenotifier.providers.bsto.entities.BsToLink
import de.cramer.releasenotifier.providers.bsto.entities.BsToSeason
import de.cramer.releasenotifier.providers.bsto.entities.BsToSeries
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.utils.Message
import de.cramer.releasenotifier.utils.collection
import de.cramer.releasenotifier.utils.select
import de.cramer.releasenotifier.utils.uri
import net.datafaker.Faker
import org.jsoup.Jsoup
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.lang.reflect.Method

@NotifierSpringBootTest
class BsToCheckerServiceTest {

    private val createMessage: Method = BsToCheckerService::class.java.getDeclaredMethod("createMessages", List::class.java)

    @MockitoBean
    private lateinit var repository: BsToSeriesRepository

    @MockitoBean
    private lateinit var seriesService: BsToSeriesService

    @Autowired
    private lateinit var htmlMessageGenerator: HtmlMessageGenerator

    private lateinit var checkerService: BsToCheckerService

    @BeforeEach
    fun setup() {
        checkerService = BsToCheckerService(repository, seriesService, htmlMessageGenerator)
    }

    @ParameterizedTest
    @MethodSource("generateTestInput")
    fun `multiple messages should be create for new episodes of muliple series`(series: List<BsToSeries>) {
        val newEpisodes = series.flatMap { it.seasons }.flatMap { it.episodes }
        val messages = createMessages(newEpisodes)
        assertThat(messages).hasSameSizeAs(series)
        assertThat(messages).each { message ->
            message.prop(Message::html).isTrue()
            message.prop(Message::message).transform { Jsoup.parse(it) }.all {
                // contains a number of top level div elements equal to the number of seasons that contain new episodes
                // because we do not which season this message was generated for, we only check for >= 1
                select("body > div").size().isGreaterThanOrEqualTo(1)

                // contains exactly one unordered list for each top level div element
                // because we do not which season this message was generated for, we only check for >= 1
                select("body > div > ul").size().isGreaterThanOrEqualTo(1)

                // not an error message
                select("pre").isEmpty()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createMessages(newEpisodes: List<BsToEpisode>) =
        createMessage.invoke(checkerService, newEpisodes) as List<Message>

    companion object {
        @JvmStatic
        private val faker = Faker()

        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun generateTestInput(): List<List<BsToSeries>> = List(10) {
            faker.collection(1..5) { generateSeries() }
        }

        @JvmStatic
        private fun generateSeries(): BsToSeries {
            val series = BsToSeries(faker.show().play(), faker.locality().localeString(), faker.internet().uri())
            series.seasons += faker.collection(1..5) { generateSeason(series) }
            return series
        }

        @JvmStatic
        private fun generateSeason(series: BsToSeries): BsToSeason {
            val season = BsToSeason(faker.number().numberBetween(1, 5), faker.internet().uri(), series)
            season.episodes += faker.collection(5..15) { generateEpisode(season) }
            return season
        }

        @JvmStatic
        private fun generateEpisode(season: BsToSeason): BsToEpisode {
            val episode = BsToEpisode(faker.number().numberBetween(1, 24), faker.show().play(), season)
            episode.links += faker.collection(1..4) { generateLink(episode) }
            return episode
        }

        @JvmStatic
        private fun generateLink(episode: BsToEpisode): BsToLink {
            return BsToLink(0, faker.internet().domainWord(), faker.internet().uri(), episode)
        }
    }
}
