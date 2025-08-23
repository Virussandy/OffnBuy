package com.ozonic.offnbuy.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.data.AppDatabase
import com.ozonic.offnbuy.model.ApiResponse
import com.ozonic.offnbuy.model.SupportedStore
import com.ozonic.offnbuy.repository.GenerateLinkRepository
import com.ozonic.offnbuy.repository.StoresRepository
import com.ozonic.offnbuy.repository.UserDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@OptIn(ExperimentalCoroutinesApi::class)
class GenerateLinkViewModel(application: Application, authViewModel: AuthViewModel) : ViewModel() {

    private val generateLinkRepository = GenerateLinkRepository()
    private val storesRepository: StoresRepository
    private val userDataRepository: UserDataRepository

    private val _supportedStores = MutableStateFlow<List<SupportedStore>>(emptyList())
    val supportedStores: StateFlow<List<SupportedStore>> = _supportedStores.asStateFlow()

    private val _productLink = MutableStateFlow("")
    val productLink: StateFlow<String> = _productLink.asStateFlow()

    // --- THIS IS THE KEY CHANGE ---
    // The UI will now reactively collect this StateFlow.
    // It automatically switches to the correct user's data flow when they log in/out.
    val recentLinks: StateFlow<List<String>>

    // We no longer need manual pagination state in the ViewModel
    // as we will display the entire reactive list.
    val hasMoreLinks: StateFlow<Boolean> = MutableStateFlow(false)
    val isListLoading: StateFlow<Boolean> = MutableStateFlow(false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _generatedLink = MutableStateFlow<ApiResponse?>(null)
    val generatedLink: StateFlow<ApiResponse?> = _generatedLink

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        storesRepository = StoresRepository(db.supportedStoreDao())
        userDataRepository = UserDataRepository(db.favoriteDealDao(), db.generatedLinkDao(), db.userProfileDao())

        // Use flatMapLatest to reactively switch to the new user's Flow
        recentLinks = authViewModel.authState.flatMapLatest { authState ->
            if (authState is AuthState.Authenticated) {
                // If a user is logged in (anonymous or not), get their links flow
                userDataRepository.getGeneratedLinks(authState.user.uid)
                    .map { linkList -> linkList.map { it.url } } // Transform List<GeneratedLink> to List<String>
            } else {
                // If no user, provide a flow with an empty list
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


        viewModelScope.launch {
            storesRepository.getSupportedStores().collect { stores ->
                _supportedStores.value = stores
            }
        }
        viewModelScope.launch {
            storesRepository.syncSupportedStores()
        }
    }

    fun generateLink() {
        if (_productLink.value.isBlank() || _isError.value) return
        val currentUserId = userDataRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = generateLinkRepository.generateLink(_productLink.value)
                if (response.success == 1 && response.data != null && response.data.startsWith("https")) {
                    // Simply add the link to the repository.
                    // The Flow will automatically pick up the change and update the UI.
                    userDataRepository.addGeneratedLink(currentUserId, response.data)
                }
                _generatedLink.value = response
            } catch (e: Exception) {
                _generatedLink.value = ApiResponse(error = 1, message = "An unexpected error occurred.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onProductLinkChange(link: String) {
        _productLink.value = link
        _isError.value = if (link.isNotBlank()) !isValidUrl(link) else false
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val pattern = Pattern.compile("(https?://\\S+)")
            pattern.matcher(url).matches()
        } catch (e: Exception) {
            false
        }
    }

    fun clearProductLink() {
        _productLink.value = ""
    }

    fun clearGeneratedLink() {
        _generatedLink.value = null
    }
}