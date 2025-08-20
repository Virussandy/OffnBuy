package com.ozonic.offnbuy.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.data.AppDatabase
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.repository.DealsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DealDetailUiState {
    object Loading : DealDetailUiState
    data class Success(val deal: DealItem) : DealDetailUiState
    data class Error(val message: String) : DealDetailUiState
}

class DealDetailViewModel(
    application: Application,
    private val dealId: String
) : ViewModel() {

    // The repository will handle fetching from local DB or Firestore
    private val dealsRepository = DealsRepository(AppDatabase.getDatabase(application).dealDao())

    private val _uiState = MutableStateFlow<DealDetailUiState>(DealDetailUiState.Loading)
    val uiState: StateFlow<DealDetailUiState> = _uiState.asStateFlow()

    init {
        fetchDeal()
    }

    private fun fetchDeal() {
        viewModelScope.launch {
            try {
                // First, try to get the deal from the fast local database
                var deal = dealsRepository.dealDao.getDealById(dealId)

                // If not found locally, fetch from Firestore (e.g., from a notification)
                if (deal == null) {
                    deal = dealsRepository.getDealFromFirestore(dealId)
                }

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