package com.ozonic.offnbuy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.model.ApiResponse
import com.ozonic.offnbuy.repository.GenerateLinkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

// Sealed interface for clean UI state management
sealed interface ShareUiState {
    object Loading : ShareUiState
    data class Success(val response: ApiResponse) : ShareUiState
    data class Error(val message: String, val originalUrl: String) : ShareUiState
}

class ShareViewModel : ViewModel() {

    private val generateLinkRepository = GenerateLinkRepository()

    private val _uiState = MutableStateFlow<ShareUiState>(ShareUiState.Loading)
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    fun processSharedUrl(sharedText: String?) {
        val url = extractUrl(sharedText)
        if (url == null) {
            // This is the new, more specific error for invalid content.
            _uiState.value = ShareUiState.Error(
                message = "No valid URL was found in the shared content.",
                originalUrl = ""
            )
            return
        }

        viewModelScope.launch {
            val response = generateLinkRepository.generateLink(url)
            if (response.success == 1 && response.data != null && response.data.startsWith("https")) {
                _uiState.value = ShareUiState.Success(response)
            } else {
                // This is the error for unsupported stores.
                _uiState.value = ShareUiState.Error(
                    message = "This store is not supported yet.",
                    originalUrl = url
                )
            }
        }
    }

    private fun extractUrl(text: String?): String? {
        if (text == null) return null
        val pattern = Pattern.compile("(https?://\\S+)")
        val matcher = pattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(0)
        } else {
            null
        }
    }
}