package com.anleonov.index.api

interface Tokenizer {

    fun tokenize(content: String): List<Token>

}
