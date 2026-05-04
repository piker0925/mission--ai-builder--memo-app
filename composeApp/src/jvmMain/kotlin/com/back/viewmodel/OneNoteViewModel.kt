package com.back.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.back.model.*
import com.back.repository.OneNoteRepository
import com.back.repository.SettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class OneNoteViewModel(
    private val repository: OneNoteRepository = OneNoteRepository(),
    private val settingsRepository: SettingsRepository = SettingsRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(OneNoteState())
    val uiState: StateFlow<OneNoteState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.darkThemeFlow.collectLatest { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        viewModelScope.launch {
            settingsRepository.fontSizeFlow.collectLatest { size ->
                _uiState.update { it.copy(fontSize = size) }
            }
        }
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val notebooks = repository.loadAllNotebooks()
            _uiState.update { state ->
                val nextState = state.copy(
                    notebooks = notebooks,
                    isLoading = false
                )
                if (nextState.selectedNotebookIndex == -1 && notebooks.isNotEmpty()) {
                    selectNotebook(0)
                }
                nextState
            }
        }
    }

    fun selectNotebook(index: Int) {
        _uiState.update { it.copy(selectedNotebookIndex = index, selectedSectionIndex = 0, selectedPageIndex = 0) }
        loadActivePage()
    }

    fun selectSection(index: Int) {
        _uiState.update { it.copy(selectedSectionIndex = index, selectedPageIndex = 0) }
        loadActivePage()
    }

    fun selectPage(index: Int) {
        _uiState.update { it.copy(selectedPageIndex = index) }
        loadActivePage()
    }

    private fun loadActivePage() {
        val page = _uiState.value.currentPage
        if (page != null) {
            _uiState.update { 
                it.copy(
                    activePageTitle = page.title,
                    activePageContent = page.content,
                    originalContentHash = page.content.hashCode()
                ) 
            }
        } else {
            _uiState.update { 
                it.copy(activePageTitle = "", activePageContent = "", originalContentHash = 0) 
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(activePageTitle = newTitle) }
        triggerAutoSave()
    }

    fun updateContent(newContent: String) {
        _uiState.update { it.copy(activePageContent = newContent) }
        triggerAutoSave()
    }

    private fun triggerAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1000)
            saveCurrentPage()
        }
    }

    private suspend fun saveCurrentPage() {
        val page = _uiState.value.currentPage ?: return
        val title = _uiState.value.activePageTitle
        val content = _uiState.value.activePageContent
        
        try {
            repository.savePage(page, title, content)
            val notebooks = repository.loadAllNotebooks()
            _uiState.update { it.copy(notebooks = notebooks, originalContentHash = content.hashCode()) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addPage() {
        val section = _uiState.value.currentSection ?: return
        viewModelScope.launch {
            repository.createPage(section, "New Page")
            refreshAll()
        }
    }

    fun addSection() {
        val notebook = _uiState.value.currentNotebook ?: return
        viewModelScope.launch {
            repository.createSection(notebook, "New Section")
            refreshAll()
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            settingsRepository.updateDarkTheme(!_uiState.value.isDarkTheme)
        }
    }

    fun updateFontSize(newSize: Int) {
        viewModelScope.launch {
            settingsRepository.updateFontSize(newSize.coerceIn(12, 40))
        }
    }
}