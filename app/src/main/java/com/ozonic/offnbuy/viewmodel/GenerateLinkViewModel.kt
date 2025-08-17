package com.ozonic.offnbuy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.model.ApiResponse
import com.ozonic.offnbuy.repository.GenerateLinkRepository
import com.ozonic.offnbuy.util.FirebaseInitialization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class GenerateLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GenerateLinkRepository((application as FirebaseInitialization).database.generatedLinkDao())
    private val _productLink = MutableStateFlow("")
    val productLink: StateFlow<String> = _productLink.asStateFlow()

    private val _recentLinks = MutableStateFlow<List<String>>(emptyList())
    val recentLinks: StateFlow<List<String>> = _recentLinks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _generatedLink = MutableStateFlow<ApiResponse?>(null)
    val generatedLink: StateFlow<ApiResponse?> = _generatedLink

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError.asStateFlow()

    init {
        getRecentLinks()
    }

    private fun getRecentLinks() {
        viewModelScope.launch {
            repository.getRecentLinks().map { list -> list.map { it.url } }
                .collect {
                    _recentLinks.value = it
                }
        }
    }

    fun onProductLinkChange(link: String) {
        _productLink.value = link
        if (link.isNotBlank()) {
            _isError.value = !isValidUrl(link)
        } else {
            _isError.value = false
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val pattern = Pattern.compile("(https?://\\S+)")
            pattern.matcher(url).matches()
        } catch (e: Exception) {
            false
        }
    }

    fun generateLink() {
        viewModelScope.launch {
            if(_productLink.value.isNotBlank() && !_isError.value){
                _isLoading.value = true

                val response = repository.generateLink(_productLink.value)

                _generatedLink.value = response
                _isLoading.value = false
            }
        }
    }

    fun clearGeneratedLink(){
        _generatedLink.value = null
    }
}