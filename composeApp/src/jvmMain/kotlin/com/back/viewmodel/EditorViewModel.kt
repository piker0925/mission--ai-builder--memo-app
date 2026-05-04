package com.back.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.back.model.FileInfo
import com.back.repository.FileRepository
import com.back.repository.SettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File

class EditorViewModel(
    private val repository: FileRepository = FileRepository(),
    private val settingsRepository: SettingsRepository = SettingsRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditorState())
    val uiState: StateFlow<EditorState> = _uiState.asStateFlow()

    // Track auto-save jobs per tab ID
    private val autoSaveJobs = mutableMapOf<String, Job>()

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
    }

    // --- Tab Management ---
    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index.coerceIn(0, it.tabs.size - 1)) }
    }

    fun addNewTab(path: String? = null, content: String = "") {
        val newTab = TabState(
            content = content,
            filePath = path,
            fileName = path?.let { File(it).name } ?: "Untitled",
            originalContentHash = content.hashCode()
        )
        _uiState.update { 
            val newTabs = it.tabs + newTab
            it.copy(tabs = newTabs, selectedTabIndex = newTabs.size - 1)
        }
    }

    fun closeTab(index: Int) {
        val tabToClose = _uiState.value.tabs.getOrNull(index) ?: return
        
        // Cancel any pending auto-save for this tab
        autoSaveJobs[tabToClose.id]?.cancel()
        autoSaveJobs.remove(tabToClose.id)

        _uiState.update { 
            if (it.tabs.size <= 1) {
                it.copy(tabs = listOf(TabState()), selectedTabIndex = 0)
            } else {
                val newTabs = it.tabs.filterIndexed { i, _ -> i != index }
                val nextIndex = if (it.selectedTabIndex >= newTabs.size) newTabs.size - 1 else it.selectedTabIndex
                it.copy(tabs = newTabs, selectedTabIndex = nextIndex)
            }
        }
    }

    // --- Editor Logic ---
    fun updateContent(newContent: String) {
        val currentTabIndex = _uiState.value.selectedTabIndex
        val currentTab = _uiState.value.currentTab ?: return
        val tabId = currentTab.id

        _uiState.update { state ->
            val updatedTabs = state.tabs.mapIndexed { index, tab ->
                if (index == state.selectedTabIndex) {
                    val updatedTab = tab.copy(content = newContent)
                    if (updatedTab.searchState.isVisible) {
                        updatedTab.copy(searchState = computeSearchResults(newContent, updatedTab.searchState))
                    } else updatedTab
                } else tab
            }
            state.copy(tabs = updatedTabs)
        }
        
        // Tab-specific Auto-save logic
        if (currentTab.filePath != null) {
            autoSaveJobs[tabId]?.cancel()
            autoSaveJobs[tabId] = viewModelScope.launch {
                delay(3000)
                saveTabById(tabId)
            }
        }
    }

    private suspend fun saveTabById(tabId: String) {
        val tab = _uiState.value.tabs.find { it.id == tabId } ?: return
        val path = tab.filePath ?: return
        val content = tab.content

        try {
            repository.writeFile(path, content)
            _uiState.update { state ->
                val updatedTabs = state.tabs.map { 
                    if (it.id == tabId) it.copy(originalContentHash = content.hashCode()) else it 
                }
                state.copy(tabs = updatedTabs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun newFile() {
        addNewTab()
    }

    fun openFile(path: String) {
        val existingIndex = _uiState.value.tabs.indexOfFirst { it.filePath == path }
        if (existingIndex >= 0) {
            selectTab(existingIndex)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val content = repository.readFile(path)
                addNewTab(path, content)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to open file: ${e.message}") }
            }
        }
    }

    fun saveFile(path: String? = _uiState.value.currentTab?.filePath) {
        val currentTab = _uiState.value.currentTab ?: return
        val currentPath = path ?: currentTab.filePath ?: return
        val currentContent = currentTab.content
        val tabId = currentTab.id
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.writeFile(currentPath, currentContent)
                val name = File(currentPath).name
                _uiState.update { state ->
                    val updatedTabs = state.tabs.map { tab ->
                        if (tab.id == tabId) {
                            tab.copy(
                                originalContentHash = currentContent.hashCode(),
                                filePath = currentPath,
                                fileName = name
                            )
                        } else tab
                    }
                    state.copy(tabs = updatedTabs, isLoading = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to save file: ${e.message}") }
            }
        }
    }

    // --- Sidebar Logic ---
    fun loadSidebarRoot(path: String) {
        _uiState.update { it.copy(sidebarRoot = path, sidebarPathStack = listOf(path)) }
        refreshSidebar()
    }

    fun navigateInto(path: String) {
        val currentStack = _uiState.value.sidebarPathStack
        _uiState.update { it.copy(sidebarPathStack = currentStack + path) }
        refreshSidebar()
    }

    fun navigateBack() {
        val currentStack = _uiState.value.sidebarPathStack
        if (currentStack.size > 1) {
            _uiState.update { it.copy(sidebarPathStack = currentStack.dropLast(1)) }
            refreshSidebar()
        }
    }

    fun refreshSidebar() {
        val path = _uiState.value.currentSidebarPath ?: return
        viewModelScope.launch {
            val files = withContext(Dispatchers.IO) {
                val root = File(path)
                if (root.isDirectory) {
                    root.listFiles { file -> 
                        !file.isHidden && (file.isDirectory || file.extension == "txt" || file.extension == "md") 
                    }?.map { 
                        FileInfo(it.name, it.absolutePath, it.isDirectory, it.extension)
                    }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
                } else {
                    emptyList()
                }
            }
            _uiState.update { it.copy(sidebarFiles = files) }
        }
    }

    // --- Search & Replace ---
    fun toggleSearchBar(visible: Boolean) {
        updateCurrentTabSearch { it.copy(isVisible = visible) }
        if (visible) {
            val tab = _uiState.value.currentTab ?: return
            updateCurrentTabSearch(computeSearchResults(tab.content, tab.searchState))
        }
    }

    fun updateSearchQuery(query: String) {
        val tab = _uiState.value.currentTab ?: return
        val newState = tab.searchState.copy(query = query)
        updateCurrentTabSearch(computeSearchResults(tab.content, newState))
    }

    fun updateReplaceText(text: String) {
        updateCurrentTabSearch { it.copy(replaceText = text) }
    }

    private fun computeSearchResults(content: String, state: SearchState): SearchState {
        if (state.query.isEmpty()) return state.copy(results = emptyList(), currentIndex = -1)

        val results = mutableListOf<IntRange>()
        var index = content.indexOf(state.query, ignoreCase = true)
        while (index >= 0) {
            results.add(index until (index + state.query.length))
            index = content.indexOf(state.query, index + state.query.length, ignoreCase = true)
        }
        return state.copy(
            results = results,
            currentIndex = if (results.isNotEmpty()) 0 else -1
        )
    }

    private fun updateCurrentTabSearch(state: SearchState) {
        _uiState.update { s ->
            val updatedTabs = s.tabs.mapIndexed { index, tab ->
                if (index == s.selectedTabIndex) tab.copy(searchState = state) else tab
            }
            s.copy(tabs = updatedTabs)
        }
    }

    private fun updateCurrentTabSearch(block: (SearchState) -> SearchState) {
        _uiState.update { s ->
            val updatedTabs = s.tabs.mapIndexed { index, tab ->
                if (index == s.selectedTabIndex) tab.copy(searchState = block(tab.searchState)) else tab
            }
            s.copy(tabs = updatedTabs)
        }
    }

    fun nextSearchResult() {
        updateCurrentTabSearch { state ->
            if (state.results.isEmpty()) state
            else state.copy(currentIndex = (state.currentIndex + 1) % state.results.size)
        }
    }

    fun previousSearchResult() {
        updateCurrentTabSearch { state ->
            if (state.results.isEmpty()) state
            else state.copy(currentIndex = if (state.currentIndex <= 0) state.results.size - 1 else state.currentIndex - 1)
        }
    }

    fun replaceCurrent() {
        val tab = _uiState.value.currentTab ?: return
        val state = tab.searchState
        if (state.currentIndex < 0 || state.results.isEmpty()) return
        
        val range = state.results[state.currentIndex]
        val newContent = tab.content.replaceRange(range, state.replaceText)
        updateContent(newContent)
    }

    fun replaceAll() {
        val tab = _uiState.value.currentTab ?: return
        if (tab.searchState.query.isEmpty()) return
        
        val newContent = tab.content.replace(tab.searchState.query, tab.searchState.replaceText, ignoreCase = true)
        updateContent(newContent)
    }

    // --- Settings ---
    fun toggleSidebar() {
        _uiState.update { it.copy(sidebarVisible = !it.sidebarVisible) }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val nextTheme = !_uiState.value.isDarkTheme
            settingsRepository.updateDarkTheme(nextTheme)
        }
    }

    fun updateFontSize(newSize: Int) {
        val size = newSize.coerceIn(8, 72)
        viewModelScope.launch {
            settingsRepository.updateFontSize(size)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}