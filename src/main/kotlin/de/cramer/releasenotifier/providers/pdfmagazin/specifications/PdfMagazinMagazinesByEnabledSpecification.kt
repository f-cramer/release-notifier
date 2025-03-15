package de.cramer.releasenotifier.providers.pdfmagazin.specifications

import de.cramer.releasenotifier.entities.Enabler
import de.cramer.releasenotifier.providers.pdfmagazin.entities.PdfMagazinMagazine
import de.cramer.releasenotifier.providers.pdfmagazin.entities.PdfMagazinMagazine_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.io.Serial

class PdfMagazinMagazinesByEnabledSpecification(
    private val enabled: Boolean = true,
) : Specification<PdfMagazinMagazine> {
    override fun toPredicate(root: Root<PdfMagazinMagazine>, query: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder): Predicate {
        return Enabler.getPredicate(enabled, root.get(PdfMagazinMagazine_.enabler), criteriaBuilder)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4029085766334107541L
    }
}
