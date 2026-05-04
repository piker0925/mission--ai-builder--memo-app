package com.back.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository {
    private val maxFileSize = 10 * 1024 * 1024 // 10MB Limit for safety

    suspend fun readFile(path: String): String = withContext(Dispatchers.IO) {
        val file = File(path)
        
        // Security: Prevent OOM by checking file size
        if (file.length() > maxFileSize) {
            throw Exception("File is too large (Max 10MB allowed)")
        }
        
        // Security: Basic check for binary content (optional but recommended)
        // Here we just use readText which might fail or return weird chars for binary
        file.readText()
    }

    suspend fun writeFile(path: String, content: String) = withContext(Dispatchers.IO) {
        File(path).writeText(content)
    }
}