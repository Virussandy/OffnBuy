package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.data.local.model.GeneratedLinkEntity
import com.ozonic.offnbuy.data.remote.dto.ApiResponse
import com.ozonic.offnbuy.domain.model.SupportedStore
import com.ozonic.offnbuy.domain.repository.AuthRepository
import com.ozonic.offnbuy.domain.repository.LinkRepository
import com.ozonic.offnbuy.domain.repository.SupportedStoreRepository
import com.ozonic.offnbuy.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LinkViewModel(
    private val linkRepository: LinkRepository,
    private val userDataRepository: UserDataRepository,
    private val supportedStoreRepository: SupportedStoreRepository,
    private val authRepository: AuthRepository // New dependency
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _dialogState = MutableStateFlow<ApiResponse?>(null)
    val dialogState: StateFlow<ApiResponse?> = _dialogState.asStateFlow()

    val supportedStores: StateFlow<List<SupportedStore>> =
        supportedStoreRepository.getStores()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val generatedLinks: StateFlow<List<GeneratedLinkEntity>> =
        authRepository.getAuthStateStream()
            .flatMapLatest { user ->
                if (user != null) {
                    userDataRepository.getGeneratedLinks(user.uid)
                } else {
                    flowOf(emptyList()) // Return an empty list when no user is logged in
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            supportedStoreRepository.syncStores()
        }
    }

    fun generateLink(productLink: String) {
        viewModelScope.launch {
            val userId = authRepository.getAuthStateStream().first()?.uid ?: return@launch
            _isLoading.value = true
            val response = linkRepository.generateLink(productLink)
            if (response.success == 1 && response.data != null) {
                userDataRepository.addGeneratedLink(userId, response.data)
            }
            _dialogState.value = response
            _isLoading.value = false
        }
    }

    fun showDialogForLink(url: String) {
        _dialogState.value = ApiResponse(success = 1, data = url)
    }

    fun onDialogDismiss() {
        _dialogState.value = null
    }
}