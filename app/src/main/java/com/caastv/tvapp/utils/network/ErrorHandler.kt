package com.caastv.tvapp.utils.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ErrorHandler {
    private val _errorState = MutableStateFlow<ErrorState?>(null)
    val errorState: StateFlow<ErrorState?> = _errorState
    
    data class ErrorState(
        val code: Int,
        val title: String,
        val message: String,
        val isNetworkError: Boolean = false
    )

    fun setError(error: ErrorState) {
        _errorState.value = error
    }

    fun clearError() {
        _errorState.value = null
    }
}