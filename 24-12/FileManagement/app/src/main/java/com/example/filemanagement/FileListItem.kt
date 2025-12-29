package com.example.filemanagement

import java.io.File

data class FileListItem(
    val title: String,
    val file: File,
    val kind: Kind
) {
    enum class Kind {
        UP,
        DIRECTORY,
        FILE
    }
}
