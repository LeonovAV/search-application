package com.anleonov.index

import com.anleonov.index.api.DocumentIndex
import com.anleonov.index.api.DocumentIndexTrackChangesListener
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class DocumentIndexStore : DocumentIndex {

    private val tokenStore = HashMap<String, MutableSet<Int>>()

    private val listeners = CopyOnWriteArrayList<DocumentIndexTrackChangesListener>()

    private val lock = ReentrantReadWriteLock()

    override fun add(token: String, documentId: Int) {
        lock.write {
            tokenStore.getOrPut(token) { mutableSetOf() }.add(documentId)
        }
        listeners.forEach { it.onTrackedTokenAdd(token, documentId) }
    }

    override fun remove(token: String, documentId: Int) {
        val isRemoved = lock.write {
            tokenStore[token]?.remove(documentId) ?: false
        }
        if (isRemoved) {
            listeners.forEach { it.onTrackedTokenRemove(token, documentId) }
        }
    }

    override fun update(token: String, documentId: Int) {
        listeners.forEach { it.onTrackedTokenUpdate(token, documentId) }
    }

    override fun getDocumentIds(token: String): Set<Int> {
        lock.read {
            return tokenStore[token]?.toSet() ?: emptySet()
        }
    }

    override fun getDocumentIdsContains(tokenQuery: String): Set<Int> {
        lock.read {
            return tokenStore
                .asSequence()
                .filter { (token, _) -> token.contains(tokenQuery) }
                .flatMapTo(HashSet()) { entry -> entry.value }
        }
    }

    override fun findTokensByDocumentId(documentId: Int): Set<String> {
        lock.read {
            return tokenStore
                .asSequence()
                .filter { (_, documentIds) -> documentId in documentIds }
                .mapTo(HashSet()) { it.key }
        }
    }

    override fun addListener(listener: DocumentIndexTrackChangesListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: DocumentIndexTrackChangesListener) {
        listeners.remove(listener)
    }

    override fun clear() {
        lock.write {
            tokenStore.clear()
        }
    }

}
