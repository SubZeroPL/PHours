package org.linkuei.notifications

class Stack<E>: Iterable<E> {
    private val innerList = mutableListOf<E>()
    private var maxSize = 0

    val size = innerList.size

    fun maxSize(): Int {
        return this.maxSize
    }

    fun push(element: E) {
        innerList.add(element)
        this.maxSize++
    }

    fun pop() {
        if (innerList.size > 0) {
            innerList.removeAt(innerList.size - 1)
            if (innerList.size == 0) {
                this.maxSize = 0
            }
        }
    }

    fun peek(): E {
        return innerList[innerList.size]
    }

    override fun iterator(): Iterator<E> {
        return innerList.toList().iterator()
    }
}