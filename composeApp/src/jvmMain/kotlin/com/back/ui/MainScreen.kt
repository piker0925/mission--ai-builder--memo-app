package com.back.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.back.ui.components.Sidebar
import com.back.ui.components.TabBar
import com.back.viewmodel.EditorViewModel

@Composable
fun MainScreen(viewModel: EditorViewModel, onTabRequestClose: (Int) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    MaterialTheme(
        colorScheme = if (uiState.isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .onKeyEvent { 
                        if (it.key == Key.Escape && it.type == KeyEventType.KeyDown) {
                            viewModel.toggleSearchBar(false)
                            true
                        } else false
                    }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Bar
                    SearchBar(uiState.currentTab?.searchState ?: com.back.viewmodel.SearchState(), viewModel)

                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        // Sidebar
                        Sidebar(uiState, viewModel)

                        // Main Editor Content
                        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            // Tab Bar
                            TabBar(
                                tabs = uiState.tabs,
                                selectedIndex = uiState.selectedTabIndex,
                                onTabSelected = { viewModel.selectTab(it) },
                                onTabClosed = onTabRequestClose,
                                onNewTab = { viewModel.addNewTab() }
                            )

                            // Editor Area
                            val currentTab = uiState.currentTab
                            if (currentTab != null) {
                                TextField(
                                    value = currentTab.content,
                                    onValueChange = { viewModel.updateContent(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    textStyle = TextStyle(
                                        fontSize = uiState.fontSize.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    placeholder = { Text("Start typing...") },
                                    visualTransformation = EditorVisualTransformation(
                                        currentTab.searchState,
                                        MaterialTheme.colorScheme
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RectangleShape
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No tab open. Create a new file.")
                                }
                            }
                            
                            // Status Bar
                            AppStatusBar(uiState)
                        }
                    }
                }

                // Error / Loading
                if (uiState.errorMessage != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.clearError() },
                        title = { Text("Error") },
                        text = { Text(uiState.errorMessage!!) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                        }
                    )
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun AppStatusBar(state: com.back.viewmodel.EditorState) {
    val currentTab = state.currentTab
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentTab?.isUnsaved == true) "* ${currentTab.fileName}" else currentTab?.fileName ?: "No file",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Chars: ${currentTab?.content?.length ?: 0} | ${state.fontSize}px",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}