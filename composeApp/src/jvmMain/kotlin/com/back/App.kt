package com.back

import androidx.compose.runtime.Composable
import com.back.ui.MainScreen
import com.back.viewmodel.EditorViewModel

@Composable
fun App(viewModel: EditorViewModel, onTabRequestClose: (Int) -> Unit) {
    MainScreen(viewModel, onTabRequestClose)
}