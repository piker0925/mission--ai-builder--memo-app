package com.back

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.back.ui.OneNoteMainScreen
import com.back.viewmodel.OneNoteViewModel

fun main() = application {
    // Correct way to initialize ViewModel in Compose Desktop to prevent crash
    val viewModel = remember { OneNoteViewModel() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "My Notepad - OneNote Style",
    ) {
        OneNoteMainScreen(viewModel)
    }
}