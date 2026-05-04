package com.back.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.back.viewmodel.EditorState
import com.back.viewmodel.EditorViewModel
import java.io.File

@Composable
fun Sidebar(
    state: EditorState,
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.sidebarVisible,
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        Surface(
            modifier = modifier.width(250.dp).fillMaxHeight(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.sidebarPathStack.size > 1) {
                        IconButton(
                            onClick = { viewModel.navigateBack() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = state.currentSidebarPath?.let { File(it).name } ?: "Explorer",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                LazyColumn {
                    items(state.sidebarFiles.size) { index ->
                        val file = state.sidebarFiles[index]
                        TextButton(
                            onClick = { 
                                if (file.isDirectory) {
                                    viewModel.navigateInto(file.path)
                                } else {
                                    viewModel.openFile(file.path)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (file.isDirectory) "📁 " else "📄 ",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    file.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}