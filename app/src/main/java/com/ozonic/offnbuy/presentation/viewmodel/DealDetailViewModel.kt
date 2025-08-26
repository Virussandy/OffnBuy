package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.domain.model.Deal
import com.ozonic.offnbuy.domain.repository.DealsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * A sealed interface for the different UI states of the deal detail screen.
 * This is defined at the top level of the file for easy access from the UI.
 */
sealed interface DealDetailUiState {
    object Loading : DealDetailUiState
    data class Success(val deal: Deal) : DealDetailUiState
    data class Error(val message: String) : DealDetailUiState
}

/**
 * ViewModel for the deal detail screen.
 * This ViewModel handles fetching a specific deal by its ID.
 *
 * @param dealId The ID of the deal to fetch.
 * @param dealsRepository The repository for accessing deal data.
 */
class DealDetailViewModel(
    private val dealId: String,
    private val dealsRepository: DealsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DealDetailUiState>(DealDetailUiState.Loading)
    val uiState: StateFlow<DealDetailUiState> = _uiState.asStateFlow()

    init {
        fetchDeal()
    }

    private fun fetchDeal() {
        viewModelScope.launch {
            try {
                // Fetch the deal directly from Firestore.
                val deal = dealsRepository.getDealFromFirestore(dealId)

                if (deal != null) {
                    _uiState.value = DealDetailUiState.Success(deal)
                } else {
                    _uiState.value = DealDetailUiState.Error("Deal not found.")
                }
            } catch (e: Exception) {
                _uiState.value = DealDetailUiState.Error("Failed to load deal details.")
            }
        }
    }
}