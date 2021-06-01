package com.anleonov.searcher.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditSetCalculatorTest {

    @Test
    fun `should calculate for empty sets`() {
        val before = emptyList<Int>()
        val after = emptyList<String>()

        val diff = EditSetCalculator(
            before,
            after,
            beforeKey = Int::toString,
            afterKey = { it }
        ).calculate()

        assertTrue(diff.operations.isEmpty())
    }

    @Test
    fun `should calculate properly`() {
        val before = listOf(1, 2, 3)
        val after = listOf("1", "3", "5")

        val diff = EditSetCalculator(
            before,
            after,
            beforeKey = Int::toString,
            afterKey = { it }
        ).calculate()

        val expectedInsertOperations = listOf(
            InsertOperation<Int, String>("5")
        )
        assertEquals(expectedInsertOperations, diff.operations.filterIsInstance<InsertOperation<Int, String>>())

        val expectedDeleteOperations = listOf(
            DeleteOperation<Int, String>(2)
        )
        assertEquals(expectedDeleteOperations, diff.operations.filterIsInstance<DeleteOperation<Int, String>>())

        val expectedKeepOperations = listOf(
            KeepOperation(1, "1"),
            KeepOperation(3, "3"),
        ).sortedBy { it.oldObject }
        assertEquals(expectedKeepOperations, diff.operations.filterIsInstance<KeepOperation<Int, String>>().sortedBy { it.oldObject })
    }

}
