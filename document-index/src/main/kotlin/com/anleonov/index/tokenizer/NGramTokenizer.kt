package com.anleonov.index.tokenizer

import com.anleonov.index.api.Token
import com.anleonov.index.api.Tokenizer

class NGramTokenizer : Tokenizer {

    override fun tokenize(content: String): List<Token> {
        return listOf(Token(content))
    }

}
