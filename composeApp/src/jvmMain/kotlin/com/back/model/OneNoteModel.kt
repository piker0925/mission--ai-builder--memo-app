package com.back.model

import java.io.File

/**
 * Simplified OneNote Hierarchy
 */

data class NotePage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val filePath: String,
    val lastModified: Long = System.currentTimeMillis()
)

data class NoteSection(
    val name: String,
    val path: String,
    val pages: List<NotePage> = emptyList()
)

data class NoteNotebook(
    val name: String,
    val path: String,
    val sections: List<NoteSection> = emptyList()
)

data class OneNoteState(
    val notebooks: List<NoteNotebook> = emptyList(),
    val selectedNotebookIndex: Int = -1,
    val selectedSectionIndex: Int = -1,
    val selectedPageIndex: Int = -1,
    
    // UI states
    val isDarkTheme: Boolean = false,
    val fontSize: Int = 16,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    
    // Active editor state
    val activePageTitle: String = "",
    val activePageContent: String = "",
    val originalContentHash: Int = 0
) {
    val currentNotebook: NoteNotebook? get() = notebooks.getOrNull(selectedNotebookIndex)
    val currentSection: NoteSection? get() = currentNotebook?.sections?.getOrNull(selectedSectionIndex)
    val currentPage: NotePage? get() = currentSection?.pages?.getOrNull(selectedPageIndex)
    val isUnsaved: Boolean get() = activePageContent.hashCode() != originalContentHash || activePageTitle != (currentPage?.title ?: "")
}