package de.cramer.releasenotifier.services

abstract class SimpleAbstractCheckerService<T, U> : AbstractCheckerService<T, U>() {
    override fun initializeState(elements: List<T>): State<T, U> = SimpleState(elements)

    protected abstract fun getChildren(t: T): List<U>

    private inner class SimpleState(
        elements: List<T>,
    ) : State<T, U> {
        private val childrenBeforeUpdate = elements.flatMap { getChildren(it) }.toSet()

        override fun getNewChildren(elements: List<T>): List<U> {
            val childrenAfterUpdate = elements.flatMap { getChildren(it) }
            return childrenAfterUpdate - childrenBeforeUpdate
        }
    }
}
