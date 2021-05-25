package com.anleonov.index

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch

class DocumentIndexStoreTest {

    private val logger = LoggerFactory.getLogger(DocumentIndexStoreTest::class.java)

    private val indexStore = DocumentIndexStore()

    companion object {
        const val DOCUMENT_1 = 1
        const val DOCUMENT_2 = 2
        const val DOCUMENT_3 = 3
    }

    @Test
    fun `should add token to index`() {
        val token = "t1"
        indexStore.add(token, DOCUMENT_1)

        assertEquals(setOf(DOCUMENT_1), indexStore.getDocumentIds(token))
    }

    @Test
    fun `should add tokens and update document list`() {
        val token = "s1"
        indexStore.add(token, DOCUMENT_1)
        indexStore.add(token, DOCUMENT_2)

        assertEquals(setOf(DOCUMENT_1, DOCUMENT_2), indexStore.getDocumentIds(token))
    }

    @Test
    fun `should add two different tokens for the same document`() {
        val token1 = "p1"
        indexStore.add(token1, DOCUMENT_1)
        val token2 = "p2"
        indexStore.add(token2, DOCUMENT_1)

        assertEquals(setOf(DOCUMENT_1), indexStore.getDocumentIds(token1))
        assertEquals(setOf(DOCUMENT_1), indexStore.getDocumentIds(token2))
    }

    @Test
    fun `should add the same token with the same document id only once`() {
        val token = "l1"
        indexStore.add(token, DOCUMENT_1)
        indexStore.add(token, DOCUMENT_1)

        assertEquals(setOf(DOCUMENT_1), indexStore.getDocumentIds(token))
    }

    @Test
    fun `should remove existing token with documents`() {
        val token = "j1"
        indexStore.add(token, DOCUMENT_1)
        indexStore.add(token, DOCUMENT_2)
        indexStore.add(token, DOCUMENT_3)

        indexStore.remove(token, DOCUMENT_1)

        assertEquals(setOf(DOCUMENT_2, DOCUMENT_3), indexStore.getDocumentIds(token))
    }

    @Test
    fun `should not remove existing token with document not in list`() {
        val token = "h1"
        indexStore.add(token, DOCUMENT_1)
        indexStore.add(token, DOCUMENT_2)

        indexStore.remove(token, DOCUMENT_3)

        assertEquals(setOf(DOCUMENT_1, DOCUMENT_2), indexStore.getDocumentIds(token))
    }

    @Test
    fun `should not remove existing token with empty document list`() {
        val token = "k1"
        indexStore.add(token, DOCUMENT_1)

        indexStore.remove(token, DOCUMENT_1)
        indexStore.remove(token, DOCUMENT_1)

        assertEquals(setOf<Int>(), indexStore.getDocumentIds(token))
    }

    @Test
    fun `should add the same token for different documents concurrently`() {
        val token = "m1"

        val latch = CountDownLatch(1)

        val t1 = Thread(Runnable {
            addTokenWithWaiter(token, DOCUMENT_1, latch)
        })

        val t2 = Thread(Runnable {
            addTokenWithWaiter(token, DOCUMENT_2, latch)
        })

        t1.start()
        t2.start()

        latch.countDown()

        t1.join()
        t2.join()

        assertEquals(setOf(DOCUMENT_1, DOCUMENT_2), indexStore.getDocumentIds(token))
    }

    @Test
    fun `should remove token during obtaining tokens for document`() {
        // Add tokens for document
        for (i in 1..1000) {
            indexStore.add("a$i", DOCUMENT_1)
        }

        val latch = CountDownLatch(1)

        val readingThreads = IntRange(1, 5).map {
            Thread(Runnable {
                try {
                    latch.await()
                } catch (ex: InterruptedException) {
                    // Ignore exception
                }
                for (i in 1..1000) {
                    val tokens = indexStore.findTokensByDocumentId(DOCUMENT_1)
                    logger.info("Tokens size for document ${tokens.size}")
                }
            })
        }

        val removingThread = Thread(Runnable {
            try {
                latch.await()
            } catch (ex: InterruptedException) {
                // Ignore exception
            }
            for (i in 1..1000) {
                indexStore.remove("a$i", DOCUMENT_1)
            }
        })

        readingThreads.forEach { it.start() }
        removingThread.start()

        latch.countDown()

        readingThreads.forEach { it.join() }
        removingThread.join()

        for (i in 1..1000) {
            assertTrue(indexStore.getDocumentIds("a$i")!!.isEmpty())
        }
    }

    private fun addTokenWithWaiter(token: String, documentId: Int, latch: CountDownLatch) {
        try {
            latch.await()
        } catch (ex: InterruptedException) {
            // Ignore exception
        }
        for (i in 1..100) {
            indexStore.add(token, documentId)
        }
    }

}
