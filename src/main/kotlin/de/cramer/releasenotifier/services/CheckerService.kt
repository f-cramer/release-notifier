package de.cramer.releasenotifier.services

import de.cramer.releasenotifier.utils.Message
import org.springframework.transaction.annotation.Transactional

interface CheckerService {
    fun check(): CheckResult
}

data class CheckResult(
    val messages: List<Message>,
    val exceptions: List<Throwable>,
)

abstract class AbstractCheckerService<T, U> : CheckerService {
    @Transactional
    override fun check(): CheckResult {
        val elements = findAll()
        val state = initializeState(elements)

        val exceptions = elements.mapNotNull { runCatching { update(it) }.exceptionOrNull() }

        val newChildren = state.getNewChildren(elements)
        return CheckResult(createMessages(newChildren), exceptions)
    }

    protected abstract fun initializeState(elements: List<T>): State<T, U>

    protected abstract fun findAll(): List<T>

    protected abstract fun update(t: T)

    protected abstract fun createMessages(newChildren: List<U>): List<Message>

    protected interface State<T, U> {
        fun getNewChildren(elements: List<T>): List<U>
    }
}
