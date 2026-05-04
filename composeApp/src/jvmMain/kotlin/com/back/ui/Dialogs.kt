package com.back.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState

@Composable
fun UnsavedChangesDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDontSave: () -> Unit
) {
    DialogWindow(
        onCloseRequest = onDismiss,
        title = "Unsaved Changes",
        state = rememberDialogState(width = 400.dp, height = 200.dp)
    ) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "You have unsaved changes. Would you like to save before proceeding?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = onDontSave) {
                        Text("Don't Save")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}