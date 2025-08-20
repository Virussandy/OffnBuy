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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class GenerateLinkViewModel(
    application: Application,
    private val authViewModel: AuthViewModel // Dependency to know the current user
) : ViewModel() {

    // The repository now needs the DAO for its own operations
    private val generateLinkRepository = GenerateLinkRepository()

    private val storesRepository: StoresRepository

    // A dedicated repository for all user-specific data
    private val userDataRepository = UserDataRepository(
        AppDatabase.getDatabase(application).favoriteDealDao(),
        AppDatabase.getDatabase(application).generatedLinkDao()
    )

    private val _supportedStores = MutableStateFlow<List<SupportedStore>>(emptyList())
    val supportedStores: StateFlow<List<SupportedStore>> = _supportedStores.asStateFlow()

    private val _productLink = MutableStateFlow("")
    val productLink: StateFlow<String> = _productLink.asStateFlow()

    // This now intelligently fetches links for the currently logged-in user.
    private val _recentLinks = MutableStateFlow<List<String>>(emptyList())
    val recentLinks: StateFlow<List<String>> = _recentLinks.asStateFlow()

    private var currentPage = 0
    private val _hasMoreLinks = MutableStateFlow(true)
    val hasMoreLinks: StateFlow<Boolean> = _hasMoreLinks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _generatedLink = MutableStateFlow<ApiResponse?>(null)
    val generatedLink: StateFlow<ApiResponse?> = _generatedLink

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError.asStateFlow()

    private val _isListLoading = MutableStateFlow(false)
    val isListLoading: StateFlow<Boolean> = _isListLoading.asStateFlow()


    init {
        val db = AppDatabase.getDatabase(application)
        storesRepository = StoresRepository(db.supportedStoreDao())
        viewModelScope.launch {
            // Start listening to the local database immediately
            storesRepository.getSupportedStores().collect { stores ->
                _supportedStores.value = stores
            }
        }
        // Sync with Firestore in the background
        viewModelScope.launch {
            storesRepository.syncSupportedStores()
        }
        // Load the first page when the ViewModel is created
        loadMoreLinks()
    }

    fun loadMoreLinks() {
        if (!_hasMoreLinks.value || _isListLoading.value) return // Use the new flag

        viewModelScope.launch {
            _isListLoading.value = true // Use the new flag
            val newLinks = userDataRepository.getGeneratedLinksPaginated(currentPage)
            if (newLinks.isNotEmpty()) {
                _recentLinks.value = _recentLinks.value + newLinks.map { it.url }
                currentPage++
            } else {
                _hasMoreLinks.value = false
            }
            _isListLoading.value = false // Use the new flag
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

    fun generateLink() {
        if (_productLink.value.isBlank() || _isError.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Generate the link via the API
                var response = generateLinkRepository.generateLink(_productLink.value)

                if (response.success == 1 && response.data != null && response.data.startsWith("https")) {
                    // Use the UserDataRepository to save the link to the current user's account
                    userDataRepository.addGeneratedLink(response.data)
                    _recentLinks.value = emptyList()
                    currentPage = 0
                    _hasMoreLinks.value = true // Also reset the "has more" flag

                    // Reload the first page from the database.
                    // Since the database sorts by creation date, the new link will now be at the top.
                    loadMoreLinks()
                }
                _generatedLink.value = response
            } catch (e: Exception) {
                _generatedLink.value = ApiResponse(error = 1, message = "An unexpected error occurred.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearProductLink(){
        _productLink.value = ""
    }

    fun clearGeneratedLink() {
        _generatedLink.value = null
    }
}