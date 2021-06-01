package com.anleonov.index

import com.anleonov.index.api.DocumentIndex
import com.anleonov.index.api.DocumentIndexTrackChangesListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

class DocumentIndexStore : DocumentIndex {

    private val tokenStore = ConcurrentHashMap<String, MutableSet<Int>>()

    private val listeners = CopyOnWriteArrayList<DocumentIndexTrackChangesListener>()

    private val lock = ReentrantReadWriteLock()
    private val readLock = lock.readLock()
    private val writeLock = lock.writeLock()

    override fun add(token: String, documentId: Int) {
        doWithLock(writeLock) {
            tokenStore.getOrPut(token) { mutableSetOf() }.add(documentId)
        }
        listeners.forEach { it.onTrackedTokenAdd(token, documentId) }
    }

    override fun remove(token: String, documentId: Int) {
        var isRemoved = false
        doWithLock(writeLock) {
            if (tokenStore.containsKey(token)) {
                tokenStore.getValue(token).remove(documentId)
                isRemoved = true
            }
        }
        if (isRemoved) {
            listeners.forEach { it.onTrackedTokenRemove(token, documentId) }
        }
    }

    override fun update(token: String, documentId: Int) {
        listeners.forEach { it.onTrackedTokenUpdate(token, documentId) }
    }

    override fun getDocumentIds(token: String): Set<Int> {
        var documentIds = emptySet<Int>()
        doWithLock(readLock) {
            if (tokenStore.containsKey(token)) {
                documentIds = tokenStore.getValue(token).toSet()
            }
        }
        return documentIds
    }

    override fun getDocumentIdsContains(tokenQuery: String): Set<Int> {
        val result = mutableSetOf<Int>()
        doWithLock(readLock) {
            tokenStore.forEach { (token: String, documentIds: MutableSet<Int>) ->
                if (token.contains(tokenQuery)) {
                    result.addAll(documentIds)
                }
            }
        }
        return result
    }

    override fun findTokensByDocumentId(documentId: Int): Set<String> {
        val tokens = mutableSetOf<String>()
        doWithLock(readLock) {
            tokenStore.forEach { (token, documentIds) ->
                if (documentId in documentIds) {
                    tokens.add(token)
                }
            }
        }
        return tokens
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

    private fun doWithLock(lock: Lock, block: () -> Unit) {
        lock.lock()
        try {
            block.invoke()
        } finally {
            lock.unlock()
        }
    }

}
