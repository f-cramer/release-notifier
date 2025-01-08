package de.cramer.releasenotifier.entities

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Embeddable
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import java.time.LocalDate

/**
 * ATTENTION: Class needs to be named so that its name is lexicographically smaller than any of its subclasses
 */
@Embeddable
@DiscriminatorColumn(name = "enabler_type")
abstract class Enabler {
    abstract val enabled: Boolean

    companion object {
        @JvmStatic
        fun getPredicate(enabled: Boolean, path: Path<Enabler>, criteriaBuilder: CriteriaBuilder): Predicate {
            val booleanPredicate = run {
                val downcast = criteriaBuilder.treat(path, ZBooleanEnabler::class.java)
                criteriaBuilder.isTrue(downcast.get(ZBooleanEnabler_.enabled))
            }

            val dateBetweenPredicate = run {
                val downcast = criteriaBuilder.treat(path, ZDateBetweenEnabler::class.java)
                val today = LocalDate.now()

                val start = downcast.get(ZDateBetweenEnabler_.start)
                val startPredicate = criteriaBuilder.or(criteriaBuilder.isNull(start), criteriaBuilder.lessThanOrEqualTo(start, today))
                val end = downcast.get(ZDateBetweenEnabler_.end)
                val endPredicate = criteriaBuilder.or(criteriaBuilder.isNull(end), criteriaBuilder.greaterThanOrEqualTo(end, today))
                // null for both parameters would lead to a null Enabler object, even if the descriminator column is "date_between"
                val notBothNullPredicate = criteriaBuilder.not(criteriaBuilder.and(criteriaBuilder.isNull(start), criteriaBuilder.isNull(end)))
                criteriaBuilder.and(startPredicate, endPredicate, notBothNullPredicate)
            }

            val predicate = criteriaBuilder.or(booleanPredicate, dateBetweenPredicate)
            return if (enabled) predicate else criteriaBuilder.not(predicate)
        }
    }
}

@Embeddable
@DiscriminatorValue("boolean")
class ZBooleanEnabler(
    @Column(name = "enabler_boolean_value")
    override val enabled: Boolean = true,
) : Enabler() {
    companion object {
        val TRUE = ZBooleanEnabler(true)
    }
}

@Embeddable
@DiscriminatorValue("date_between")
class ZDateBetweenEnabler(
    @Column(name = "enabler_date_between_start")
    val start: LocalDate?,
    @Column(name = "enabler_date_between_end")
    val end: LocalDate?,
) : Enabler() {
    override val enabled: Boolean
        get() {
            val today = LocalDate.now()
            return (start == null || start <= today) && (end == null || end >= today)
        }
}
