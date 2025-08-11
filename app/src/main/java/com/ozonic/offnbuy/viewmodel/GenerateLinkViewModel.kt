package com.ozonic.offnbuy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.model.ApiResponse
import com.ozonic.offnbuy.repository.GenerateLinkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GenerateLinkViewModel : ViewModel() {

    private val repository = GenerateLinkRepository()
    private val _productLink = MutableStateFlow("")
    val productLink: MutableStateFlow<String> = _productLink

    private val _recentLinks = MutableStateFlow<List<String>>(emptyList())
    val recentLinks: MutableStateFlow<List<String>> = _recentLinks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: MutableStateFlow<Boolean> = _isLoading

    private val _generatedLink = MutableStateFlow<ApiResponse?>(null)
    val generatedLink: MutableStateFlow<ApiResponse?> = _generatedLink

    init {
        getRecentLinks()
    }

    fun getRecentLinks() {
        _recentLinks.value = listOf<String>("https://amzn.to/3Hrnblw", "https://amzn.to/4fx0mK3")
    }

    fun onProductLinkChange(link: String) {
        _productLink.value = link
    }

    fun generateLink() {
        viewModelScope.launch {
            if(_productLink.value.isNotBlank()){
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