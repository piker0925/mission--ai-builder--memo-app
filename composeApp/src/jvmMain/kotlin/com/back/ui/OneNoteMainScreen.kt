package com.back.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.back.viewmodel.OneNoteViewModel

@Composable
fun OneNoteMainScreen(viewModel: OneNoteViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    MaterialTheme(
        colorScheme = if (uiState.isDarkTheme) darkColorScheme() else lightColorScheme(),
        typography = getTypography()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Tier 1: Notebooks & Sections
                Surface(
                    modifier = Modifier.width(220.dp).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Notebooks",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                        )
                        
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            itemsIndexed(uiState.notebooks) { index, notebook ->
                                NavigationItem(
                                    label = notebook.name,
                                    icon = Icons.Default.Folder,
                                    isSelected = uiState.selectedNotebookIndex == index,
                                    onClick = { viewModel.selectNotebook(index) }
                                )
                                
                                if (uiState.selectedNotebookIndex == index) {
                                    Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                                        notebook.sections.forEachIndexed { sIndex, section ->
                                            NavigationItem(
                                                label = section.name,
                                                icon = Icons.Default.Notes,
                                                isSelected = uiState.selectedSectionIndex == sIndex,
                                                onClick = { viewModel.selectSection(sIndex) },
                                                isCompact = true
                                            )
                                        }
                                        TextButton(
                                            onClick = { viewModel.addSection() },
                                            modifier = Modifier.fillMaxWidth().height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Add Section", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                        
                        IconButton(onClick = { viewModel.toggleTheme() }) {
                            Icon(if (uiState.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null)
                        }
                    }
                }

                // Tier 2: Page List
                Surface(
                    modifier = Modifier.width(260.dp).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                text = uiState.currentSection?.name ?: "Pages",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            uiState.currentSection?.let { section ->
                                itemsIndexed(section.pages) { index, page ->
                                    PageListItem(
                                        title = page.title,
                                        snippet = page.content.take(60).replace("\n", " "),
                                        isSelected = uiState.selectedPageIndex == index,
                                        onClick = { viewModel.selectPage(index) }
                                    )
                                }
                            }
                        }
                        
                        Button(
                            onClick = { viewModel.addPage() },
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("New Page")
                        }
                    }
                }

                // Tier 3: Editor
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (uiState.currentPage != null) {
                        TextField(
                            value = uiState.activePageTitle,
                            onValueChange = { viewModel.updateTitle(it) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                            textStyle = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            placeholder = { Text("Untitled Page") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 40.dp), thickness = 1.dp)

                        TextField(
                            value = uiState.activePageContent,
                            onValueChange = { viewModel.updateContent(it) },
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp, vertical = 8.dp),
                            textStyle = TextStyle(fontSize = uiState.fontSize.sp, lineHeight = 24.sp),
                            placeholder = { Text("Start typing...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (uiState.isUnsaved) Icons.Default.Sync else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.isUnsaved) "Saving..." else "All changes saved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Description, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(Modifier.height(16.dp))
                                Text("Select a page to begin", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    isCompact: Boolean = false
) {
    val height = if (isCompact) 36.dp else 44.dp
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(if (isCompact) 18.dp else 20.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PageListItem(
    title: String,
    snippet: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)) {
            Text(
                text = if (title.isEmpty()) "Untitled" else title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (snippet.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}