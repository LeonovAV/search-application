package com.anleonov.index.tokenizer

import com.anleonov.index.api.Token
import com.anleonov.index.api.Tokenizer
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class NGramTokenizer(
    private val nGramSize: Int
) : Tokenizer {

    override fun tokenize(content: String): List<Token> {
        if (content.isEmpty()) return emptyList()

        if (content.length <= nGramSize) return listOf(Token(content.toLowerCase()))

        val tokens = mutableListOf<Token>()
        for (i in 0..content.length - nGramSize) {
            val tokenContent = prepareTokenContent(content, i)
            tokens.add(Token(tokenContent))
        }

        logger.trace { "Extracted tokens: $tokens" }

        return tokens
    }

    private fun prepareTokenContent(content: String, currentPosition: Int): String {
        return content.substring(
            startIndex = currentPosition,
            endIndex = currentPosition + nGramSize
        ).toLowerCase()
    }

}
