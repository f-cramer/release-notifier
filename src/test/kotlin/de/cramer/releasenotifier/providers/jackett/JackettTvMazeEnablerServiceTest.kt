package de.cramer.releasenotifier.providers.jackett

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import de.cramer.releasenotifier.entities.Enabler
import de.cramer.releasenotifier.entities.ZBooleanEnabler
import de.cramer.releasenotifier.entities.ZDateBetweenEnabler
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import de.cramer.releasenotifier.providers.tvmaze.TvMazeService
import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeIntegration
import de.cramer.releasenotifier.utils.uri
import net.datafaker.Faker
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

class JackettTvMazeEnablerServiceTest {

    private val searchRepository: JackettSearchRepository = mock(JackettSearchRepository::class.java)

    private val tvMazeService: TvMazeService = mock(TvMazeService::class.java)

    private val service = JackettTvMazeEnablerService(searchRepository, tvMazeService, LoggerFactory.getLogger(JackettTvMazeEnablerService::class.java))

    @Test
    fun `disabled search is switched to a date based enabler when the air date of its next episode is known`() {
        val search = generateSearch(ZBooleanEnabler(false))
        val nextEpisodeAirDate = LocalDate.now().plusDays(7)
        findAllReturns(search)
        `when`(tvMazeService.getNextEpisodeAirDate(search.tvMazeIntegration!!)).thenReturn(nextEpisodeAirDate)

        service.updateEnablers()

        assertThat(search.enabler).isInstanceOf<ZDateBetweenEnabler>().all {
            prop(ZDateBetweenEnabler::start).isEqualTo(nextEpisodeAirDate)
            prop(ZDateBetweenEnabler::end).isNull()
        }
    }

    @Test
    fun `enabled search is not touched when the air date of its next episode is known`() {
        val enabler = ZDateBetweenEnabler(LocalDate.now().minusDays(14), null)
        val search = generateSearch(enabler)
        findAllReturns(search)
        `when`(tvMazeService.getNextEpisodeAirDate(search.tvMazeIntegration!!)).thenReturn(LocalDate.now().plusDays(7))

        service.updateEnablers()

        assertThat(search.enabler).isEqualTo(enabler)
    }

    @Test
    fun `disabled search stays disabled when the air date of its next episode is unknown`() {
        val search = generateSearch(ZBooleanEnabler(false))
        findAllReturns(search)

        service.updateEnablers()

        assertThat(search.enabler).isInstanceOf<ZBooleanEnabler>()
            .prop(ZBooleanEnabler::enabled).isFalse()
        verify(tvMazeService, never()).getCurrentSeasonEndDate(search.tvMazeIntegration!!)
    }

    @Test
    fun `enabled search is disabled when its current season ended and the air date of its next episode is unknown`() {
        val search = generateSearch(ZDateBetweenEnabler(LocalDate.now().minusDays(90), null))
        val seasonEndDate = LocalDate.now().minusDays(3)
        findAllReturns(search)
        `when`(tvMazeService.getCurrentSeasonEndDate(search.tvMazeIntegration!!)).thenReturn(seasonEndDate)

        service.updateEnablers()

        assertThat(search.enabler).isInstanceOf<ZBooleanEnabler>()
            .prop(ZBooleanEnabler::enabled).isFalse()
    }

    @Test
    fun `enabled search is not disabled while its current season is still within the grace period`() {
        val enabler = ZDateBetweenEnabler(LocalDate.now().minusDays(90), null)
        val search = generateSearch(enabler)
        findAllReturns(search)
        `when`(tvMazeService.getCurrentSeasonEndDate(search.tvMazeIntegration!!)).thenReturn(LocalDate.now().minusDays(2))

        service.updateEnablers()

        assertThat(search.enabler).isEqualTo(enabler)
    }

    @Test
    fun `enabled search is not disabled when the end date of its current season is unknown`() {
        val enabler = ZDateBetweenEnabler(LocalDate.now().minusDays(90), null)
        val search = generateSearch(enabler)
        findAllReturns(search)

        service.updateEnablers()

        assertThat(search.enabler).isEqualTo(enabler)
    }

    @Test
    fun `search and its tvmaze integration are disabled when the show has ended`() {
        val search = generateSearch(ZDateBetweenEnabler(LocalDate.now().minusDays(90), null))
        val integration = search.tvMazeIntegration!!
        findAllReturns(search)
        `when`(tvMazeService.getShowEndDate(integration)).thenReturn(LocalDate.now().minusDays(3))

        service.updateEnablers()

        assertThat(integration.enabled).isFalse()
        assertThat(search.enabler).isInstanceOf<ZBooleanEnabler>()
            .prop(ZBooleanEnabler::enabled).isFalse()
        verify(tvMazeService, never()).getNextEpisodeAirDate(integration)
    }

    @Test
    fun `tvmaze integration is kept while the end of the show is still within the grace period`() {
        val enabler = ZDateBetweenEnabler(LocalDate.now().minusDays(90), null)
        val search = generateSearch(enabler)
        val integration = search.tvMazeIntegration!!
        findAllReturns(search)
        `when`(tvMazeService.getShowEndDate(integration)).thenReturn(LocalDate.now().minusDays(2))
        `when`(tvMazeService.getNextEpisodeAirDate(integration)).thenReturn(LocalDate.now())

        service.updateEnablers()

        assertThat(integration.enabled).isTrue()
        assertThat(search.enabler).isEqualTo(enabler)
    }

    @Test
    fun `an error for one search does not prevent the other searches from being updated`() {
        val failingSearch = generateSearch(ZBooleanEnabler(false))
        val search = generateSearch(ZBooleanEnabler(false))
        val nextEpisodeAirDate = LocalDate.now().plusDays(7)
        findAllReturns(failingSearch, search)
        `when`(tvMazeService.getNextEpisodeAirDate(failingSearch.tvMazeIntegration!!)).thenThrow(IllegalStateException("show not found"))
        `when`(tvMazeService.getNextEpisodeAirDate(search.tvMazeIntegration!!)).thenReturn(nextEpisodeAirDate)

        service.updateEnablers()

        assertThat(failingSearch.enabler).isInstanceOf<ZBooleanEnabler>()
        assertThat(search.enabler).isInstanceOf<ZDateBetweenEnabler>()
            .prop(ZDateBetweenEnabler::start).isEqualTo(nextEpisodeAirDate)
    }

    private fun findAllReturns(vararg searches: JackettSearch) {
        `when`(searchRepository.findAll(any<Specification<JackettSearch>>())).thenReturn(searches.toList())
    }

    private fun generateSearch(enabler: Enabler): JackettSearch {
        val search = JackettSearch(faker.show().play(), faker.internet().uri())
        search.enabler = enabler
        search.tvMazeIntegration = TvMazeIntegration(showIds.incrementAndGet(), null, null)
        return search
    }

    companion object {
        @JvmStatic
        private val faker = Faker()

        @JvmStatic
        private val showIds = AtomicLong()
    }
}
