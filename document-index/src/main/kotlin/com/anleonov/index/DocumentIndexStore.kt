package com.anleonov.index

import com.anleonov.index.api.DocumentIndex
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

class DocumentIndexStore : DocumentIndex {

    private val tokenStore = ConcurrentHashMap<String, MutableSet<Int>>()

    private val lock = ReentrantLock()

    override fun add(token: String, documentId: Int) {
        doWithLock {
            tokenStore.getOrPut(token) { mutableSetOf() }.add(documentId)
        }
    }

    override fun remove(token: String, documentId: Int) {
        doWithLock {
            if (tokenStore.containsKey(token)) {
                tokenStore.getValue(token).remove(documentId)
            }
        }
    }

    override fun update(token: String, documentId: Int) {
    }

    override fun getDocumentIds(token: String): Set<Int>? {
        return if (tokenStore.containsKey(token)) {
            tokenStore.getValue(token).toSet()
        } else null
    }

    override fun findTokensByDocumentId(documentId: Int): Set<String> {
        return tokenStore.mapNotNullTo(HashSet()) { (token, documentIds) ->
            if (documentId in documentIds) {
                token
            } else null
        }
    }

    private fun doWithLock(block: () -> Unit) {
        lock.lock()
        try {
            block.invoke()
        } finally {
            lock.unlock()
        }
    }

}
