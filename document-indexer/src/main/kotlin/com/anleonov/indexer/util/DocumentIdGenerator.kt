package com.anleonov.indexer.util

import java.util.concurrent.atomic.AtomicInteger

object DocumentIdGenerator {

    private val documentId = AtomicInteger()

    fun generate(): Int {
        return documentId.incrementAndGet()
    }

    fun reset() {
        documentId.set(0)
    }

}
