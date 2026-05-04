package com.back.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.back.viewmodel.EditorViewModel
import com.back.viewmodel.SearchState

@Composable
fun SearchBar(
    state: SearchState,
    viewModel: EditorViewModel
) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Input
                    TextField(
                        value = state.query,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Find") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        trailingIcon = {
                            if (state.results.isNotEmpty()) {
                                Text(
                                    "${state.currentIndex + 1}/${state.results.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    )

                    // Navigation Buttons
                    IconButton(onClick = { viewModel.previousSearchResult() }) {
                        Icon(Icons.Default.KeyboardArrowUp, "Previous")
                    }
                    IconButton(onClick = { viewModel.nextSearchResult() }) {
                        Icon(Icons.Default.KeyboardArrowDown, "Next")
                    }
                    
                    // Close Button
                    IconButton(onClick = { viewModel.toggleSearchBar(false) }) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Replace Input
                    TextField(
                        value = state.replaceText,
                        onValueChange = { viewModel.updateReplaceText(it) },
                        placeholder = { Text("Replace with") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Button(
                        onClick = { viewModel.replaceCurrent() },
                        enabled = state.currentIndex >= 0,
                        shape = RectangleShape
                    ) {
                        Text("Replace")
                    }
                    
                    Button(
                        onClick = { viewModel.replaceAll() },
                        enabled = state.results.isNotEmpty(),
                        shape = RectangleShape
                    ) {
                        Text("All")
                    }
                }
            }
        }
    }
}