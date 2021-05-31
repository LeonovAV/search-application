package com.anleonov.index.api

import java.nio.file.Path

data class Token(
    val content: String
) {

    fun length(): Int {
        return content.length
    }

}

data class Document(
    val id: Int,
    val path: Path,
    val parentPath: Path
)

object CommonNGramSize {

    const val triGram = 3

}
