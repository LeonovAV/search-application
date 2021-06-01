package com.anleonov.searcher.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StringUtilsTest {

    @Test
    fun `should not find substring positions`() {
        assertTrue("hello".allIndicesOf("b").isEmpty())
    }

    @Test
    fun `should find all one letter positions in several string parts`() {
        val result = "abracadabra".allIndicesOf("a")
        val expectedResult = listOf(0, 3, 5, 7, 10)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `should find all substring positions in several string parts`() {
        val result = "abracadabral".allIndicesOf("bra")
        val expectedResult = listOf(1, 8)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `should find the whole string position in string`() {
        val result = "hello".allIndicesOf("hello")
        val expectedResult = listOf(0)

        assertEquals(expectedResult, result)
    }

}
