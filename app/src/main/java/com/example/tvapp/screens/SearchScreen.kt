package com.example.tvapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tvapp.viewmodels.EPGViewModel
import com.example.tvapp.models.EPGProgram
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(viewModel: EPGViewModel = viewModel()) {
    var searchText by remember { mutableStateOf("") }
    val searchResults = viewModel.searchResults.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null)}
    val keyboardController = LocalSoftwareKeyboardController.current  // Keyboard controller

    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            OutlinedTextField(
                value = searchText,
                onValueChange = { newText ->
                    searchText = newText
                    searchJob?.cancel()
                    searchJob = coroutineScope.launch {
                        delay(300)  // Debounce time delay
                        viewModel.searchPrograms(newText)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search Programs") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    // Optionally handle search action if needed
                    searchJob?.cancel()
                    coroutineScope.launch {
                        viewModel.searchPrograms(searchText)
                    }
                    keyboardController?.hide()  // Hides the keyboard on search
                })
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(searchResults) { program ->
                    ProgramItem(program)
                }
            }
        }
    }
}

@Composable
fun ProgramItem(program: EPGProgram) {
    Text(
        text = "${program.eventName} (${program.startTime} - ${program.endTime})",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
