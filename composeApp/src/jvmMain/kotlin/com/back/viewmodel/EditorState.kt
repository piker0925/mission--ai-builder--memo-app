package com.back.viewmodel

import com.back.model.FileInfo
import java.io.File

data class SearchState(
    val query: String = "",
    val results: List<IntRange> = emptyList(),
    val currentIndex: Int = -1,
    val isVisible: Boolean = false,
    val replaceText: String = ""
)

data class TabState(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String = "",
    val filePath: String? = null,
    val originalContentHash: Int = "".hashCode(),
    val fileName: String = "Untitled",
    val searchState: SearchState = SearchState()
) {
    val isUnsaved: Boolean get() = content.hashCode() != originalContentHash
}

data class EditorState(
    val tabs: List<TabState> = listOf(TabState()),
    val selectedTabIndex: Int = 0,
    val isDarkTheme: Boolean = false,
    val fontSize: Int = 14,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sidebarVisible: Boolean = true,
    val sidebarRoot: String? = null,
    val sidebarFiles: List<FileInfo> = emptyList(),
    val sidebarPathStack: List<String> = emptyList()
) {
    val currentTab: TabState? get() = tabs.getOrNull(selectedTabIndex)
    val currentSidebarPath: String? get() = sidebarPathStack.lastOrNull() ?: sidebarRoot
}