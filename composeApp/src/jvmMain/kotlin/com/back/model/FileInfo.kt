package com.back.model

data class FileInfo(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val extension: String = ""
)