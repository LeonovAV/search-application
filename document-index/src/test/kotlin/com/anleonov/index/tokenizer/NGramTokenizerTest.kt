package com.anleonov.index.tokenizer

import com.anleonov.index.api.Token
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NGramTokenizerTest {

    private val triGramTokenizer = NGramTokenizer(nGramSize = 3)

    @Test
    fun `should not create tokens for empty content`() {
        val tokens = triGramTokenizer.tokenize("")
        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `should create one token with two characters`() {
        val tokens = triGramTokenizer.tokenize("ab")

        val expectedTokens = listOf(
            Token("ab")
        )
        assertEquals(expectedTokens, tokens)
    }

    @Test
    fun `should create two tokens with the same length`() {
        val tokens = triGramTokenizer.tokenize("abcd")

        val expectedTokens = listOf(
            Token("abc"),
            Token("bcd")
        )
        assertEquals(expectedTokens, tokens)
    }

    @Test
    fun `should create several tokens in lowercase with space`() {
        val tokens = triGramTokenizer.tokenize("Hello World")

        val expectedTokens = listOf(
            Token("hel"),
            Token("ell"),
            Token("llo"),
            Token("lo "),
            Token("o w"),
            Token(" wo"),
            Token("wor"),
            Token("orl"),
            Token("rld")
        )
        assertEquals(expectedTokens, tokens)
    }

    @Test
    fun `should tokenize one trigram in lowercase`() {
        val tokens = triGramTokenizer.tokenize("Yes")

        val expectedTokens = listOf(
            Token("yes")
        )
        assertEquals(expectedTokens, tokens)
    }

}
