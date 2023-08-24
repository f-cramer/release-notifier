package de.cramer.releasenotifier.services

import de.cramer.releasenotifier.utils.Message
import org.springframework.transaction.annotation.Transactional

interface CheckerService {
    fun check(): List<Message>
}

abstract class AbstractCheckerSerivce<T, U> : CheckerService {
    @Transactional
    override fun check(): List<Message> {
        val elements = findAll()
        val childrenBeforeUpdate = elements.flatMap { getChildren(it) }

        elements.forEach { update(it) }
        val childrenAfterUpdate = elements.flatMap { getChildren(it) }

        val newChildren = childrenAfterUpdate - childrenBeforeUpdate.toSet()
        return createMessages(newChildren)
    }

    protected abstract fun findAll(): List<T>

    protected abstract fun getChildren(t: T): List<U>

    protected abstract fun update(t: T)

    protected abstract fun createMessages(newChildren: List<U>): List<Message>
}
