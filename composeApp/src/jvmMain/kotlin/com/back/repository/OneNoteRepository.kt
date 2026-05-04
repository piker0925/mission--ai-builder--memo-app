package com.back.repository

import com.back.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class OneNoteRepository {
    private val rootPath = File(System.getProperty("user.home"), "MyNotepadNotes").apply {
        if (!exists()) mkdirs()
    }

    suspend fun loadAllNotebooks(): List<NoteNotebook> = withContext(Dispatchers.IO) {
        if (rootPath.listFiles { it.isDirectory }?.isEmpty() != false) {
            val defaultNb = File(rootPath, "My Notes").apply { mkdirs() }
            File(defaultNb, "General").mkdirs()
        }

        rootPath.listFiles { it.isDirectory }?.map { notebookDir ->
            val sections = notebookDir.listFiles { it.isDirectory }?.map { sectionDir ->
                val pages = sectionDir.listFiles { it.isFile && (it.extension == "md" || it.extension == "txt") }?.map { pageFile ->
                    NotePage(
                        title = pageFile.nameWithoutExtension,
                        content = pageFile.readText(),
                        filePath = pageFile.absolutePath,
                        lastModified = pageFile.lastModified()
                    )
                }?.sortedByDescending { it.lastModified } ?: emptyList()

                NoteSection(sectionDir.name, sectionDir.absolutePath, pages)
            }?.sortedBy { it.name } ?: emptyList()

            NoteNotebook(notebookDir.name, notebookDir.absolutePath, sections)
        }?.sortedBy { it.name } ?: emptyList()
    }

    suspend fun savePage(page: NotePage, title: String, content: String): String = withContext(Dispatchers.IO) {
        val file = File(page.filePath)
        val parentDir = file.parentFile
        
        val targetFileName = if (title.isEmpty()) "Untitled" else title
        val newFile = if (targetFileName != page.title) {
            val f = File(parentDir, "$targetFileName.md")
            if (file.exists()) file.renameTo(f)
            f
        } else {
            file
        }
        
        newFile.writeText(content)
        newFile.absolutePath
    }

    suspend fun createPage(section: NoteSection, title: String): NotePage = withContext(Dispatchers.IO) {
        val file = File(section.path, "$title.md")
        file.writeText("")
        NotePage(title = title, content = "", filePath = file.absolutePath)
    }

    suspend fun createSection(notebook: NoteNotebook, name: String) = withContext(Dispatchers.IO) {
        File(notebook.path, name).mkdirs()
    }

    suspend fun createNotebook(name: String) = withContext(Dispatchers.IO) {
        File(rootPath, name).mkdirs()
    }
}