package com.anleonov.index

import com.anleonov.index.api.DocumentIndex
import com.anleonov.index.api.DocumentIndexTrackChangesListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock

class DocumentIndexStore : DocumentIndex {

    private val tokenStore = ConcurrentHashMap<String, MutableSet<Int>>()

    private val listeners = CopyOnWriteArrayList<DocumentIndexTrackChangesListener>()

    private val lock = ReentrantLock()

    override fun add(token: String, documentId: Int) {
        doWithLock {
            tokenStore.getOrPut(token) { mutableSetOf() }.add(documentId)

            // TODO think about concurrency here
            listeners.forEach { it.onTrackedTokenAdd(token, documentId) }
        }
    }

    override fun remove(token: String, documentId: Int) {
        doWithLock {
            if (tokenStore.containsKey(token)) {
                tokenStore.getValue(token).remove(documentId)

                listeners.forEach { it.onTrackedTokenRemove(token, documentId) }
            }
        }
    }

    override fun update(token: String, documentId: Int) {
        listeners.forEach { it.onTrackedTokenUpdate(token, documentId) }
    }

    override fun getDocumentIds(token: String): Set<Int> {
        return if (tokenStore.containsKey(token)) {
            tokenStore.getValue(token).toSet()
        } else emptySet()
    }

    override fun getDocumentIdsContains(tokenQuery: String): Set<Int> {
        val result = mutableSetOf<Int>()
        tokenStore.forEach { (token: String, documentIds: MutableSet<Int>) ->
            if (token.contains(tokenQuery)) {
                // TODO think about concurrency problem
                result.addAll(documentIds)
            }
        }
        return result
    }

    override fun findTokensByDocumentId(documentId: Int): Set<String> {
        return tokenStore.mapNotNullTo(HashSet()) { (token, documentIds) ->
            if (documentId in documentIds) {
                token
            } else null
        }
    }

    override fun addListener(listener: DocumentIndexTrackChangesListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: DocumentIndexTrackChangesListener) {
        listeners.remove(listener)
    }

    override fun clear() {
        tokenStore.clear()
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
