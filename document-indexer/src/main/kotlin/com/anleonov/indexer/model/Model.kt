package com.anleonov.indexer.model

import java.nio.file.Path

data class Document(
    val id: Int,
    val path: Path,
    val parentPath: Path
)
