package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.data.remote.dto.ApiResponse
import com.ozonic.offnbuy.domain.repository.LinkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * A sealed interface representing the different UI states for the Share screen.
 */
sealed interface ShareUiState {
    object Loading : ShareUiState
    data class Success(val response: ApiResponse) : ShareUiState
    data class Error(val message: String, val originalUrl: String) : ShareUiState
}

/**
 * ViewModel for the Share screen/activity.
 * This ViewModel processes text shared with the app, extracts a URL,
 * and uses the LinkRepository to generate an affiliate link.
 *
 * @param linkRepository The repository for generating links.
 */
class ShareViewModel(
    private val linkRepository: LinkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShareUiState>(ShareUiState.Loading)
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    /**
     * Processes the shared text to extract a URL and generate a link.
     *
     * @param sharedText The raw text content received from the share intent.
     */
    fun processSharedUrl(sharedText: String?) {
        val url = extractUrl(sharedText)
        if (url == null) {
            _uiState.value = ShareUiState.Error(
                message = "No valid URL was found in the shared content.",
                originalUrl = ""
            )
            return
        }

        viewModelScope.launch {
            val response = linkRepository.generateLink(url)
            if (response.success == 1 && response.data != null && response.data.startsWith("https")) {
                _uiState.value = ShareUiState.Success(response)
            } else {
                _uiState.value = ShareUiState.Error(
                    message = "This store is not supported yet.",
                    originalUrl = url
                )
            }
        }
    }

    /**
     * Extracts the first valid URL from a given string.
     *
     * @param text The text to search for a URL.
     * @return The found URL as a String, or null if no URL is found.
     */
    private fun extractUrl(text: String?): String? {
        if (text == null) return null
        // This regex pattern finds HTTP or HTTPS URLs.
        val pattern = Pattern.compile("(https?://\\S+)")
        val matcher = pattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(0)
        } else {
            null
        }
    }
}