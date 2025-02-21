package de.cramer.releasenotifier.services

import de.cramer.releasenotifier.utils.Message
import org.springframework.transaction.annotation.Transactional

interface CheckerService {
    fun check(): List<Message>
}

abstract class AbstractCheckerService<T, U> : CheckerService {
    @Transactional
    override fun check(): List<Message> {
        val elements = findAll()
        val state = initializeState(elements)

        elements.forEach { update(it) }

        val newChildren = state.getNewChildren(elements)
        return createMessages(newChildren)
    }

    protected abstract fun initializeState(elements: List<T>): State<T, U>

    protected abstract fun findAll(): List<T>

    protected abstract fun update(t: T)

    protected abstract fun createMessages(newChildren: List<U>): List<Message>

    protected interface State<T, U> {
        fun getNewChildren(elements: List<T>): List<U>
    }
}
