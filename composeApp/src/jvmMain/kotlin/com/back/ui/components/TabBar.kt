package com.back.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.back.viewmodel.TabState

@Composable
fun TabBar(
    tabs: List<TabState>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    onTabClosed: (Int) -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedIndex == index
                Surface(
                    onClick = { onTabSelected(index) },
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (tab.isUnsaved) "* ${tab.fileName}" else tab.fileName,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                        IconButton(
                            onClick = { onTabClosed(index) },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close Tab",
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // New Tab Button
        IconButton(
            onClick = onNewTab,
            modifier = Modifier.padding(horizontal = 8.dp).size(24.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "New Tab",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}