package de.cramer.releasenotifier.providers.jackett.specifications

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import de.cramer.releasenotifier.NotifierSpringBootTest
import de.cramer.releasenotifier.entities.Enabler
import de.cramer.releasenotifier.entities.ZBooleanEnabler
import de.cramer.releasenotifier.entities.ZDateBetweenEnabler
import de.cramer.releasenotifier.providers.jackett.JackettSearchRepository
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeIntegration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.LocalDate

@NotifierSpringBootTest
@Transactional
class JackettSearchesWithEnabledTvMazeIntegrationSpecificationTest {

    @Autowired
    private lateinit var repository: JackettSearchRepository

    @Test
    fun `only searches with an enabled tvmaze integration are found regardless of their enabler`() {
        createSearch("disabled-with-integration", ZBooleanEnabler(false), withIntegration = true)
        createSearch("enabled-with-integration", ZBooleanEnabler(true), withIntegration = true)
        createSearch("date-based-with-integration", ZDateBetweenEnabler(LocalDate.now().minusDays(1), null), withIntegration = true)
        createSearch("disabled-without-integration", ZBooleanEnabler(false), withIntegration = false)
        createSearch("enabled-without-integration", ZBooleanEnabler(true), withIntegration = false)
        createSearch("disabled-with-disabled-integration", ZBooleanEnabler(false), withIntegration = true, integrationEnabled = false)
        createSearch("enabled-with-disabled-integration", ZBooleanEnabler(true), withIntegration = true, integrationEnabled = false)

        val searches = repository.findAll(JackettSearchesWithEnabledTvMazeIntegrationSpecification())

        assertThat(searches.map { it.name }).containsExactlyInAnyOrder(
            "disabled-with-integration",
            "enabled-with-integration",
            "date-based-with-integration",
        )
    }

    private fun createSearch(name: String, enabler: Enabler, withIntegration: Boolean, integrationEnabled: Boolean = true) {
        val search = JackettSearch(name, URI("http://localhost/api?t=search&q=$name"))
        search.enabler = enabler
        if (withIntegration) {
            search.tvMazeIntegration = TvMazeIntegration(1, null, null, integrationEnabled)
        }
        repository.save(search)
    }
}
