package com.ozonic.offnbuy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.domain.model.NotifiedDeal
import com.ozonic.offnbuy.domain.usecase.GetNotificationsUseCase
import com.ozonic.offnbuy.util.SharedPrefManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the notification screen.
 */
class NotificationViewModel(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    sharedPrefManager: SharedPrefManager // New dependency for seen status
) : ViewModel() {


    // Combine flows to get the final list with 'isSeen' status
    val notifiedDeals: StateFlow<List<NotifiedDeal>> = combine(
        getNotificationsUseCase.execute(),
        sharedPrefManager.seenDealIdsFlow
    ) { deals, seenIds ->
        deals.map { deal ->
            deal.copy(isSeen = seenIds.contains(deal.deal_id))
        }.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // The unseen count is now a reactive flow derived from the main list.
    val unseenCount: StateFlow<Int> = notifiedDeals.map { list ->
        list.count { !it.isSeen }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Marks a notification as seen.
     *
     * @param dealId The ID of the deal to mark as seen.
     */
    fun markAsSeen(dealId: String) {
        viewModelScope.launch {
            getNotificationsUseCase.markAsSeen(dealId)
        }
    }

    /**
     * Marks all notifications as seen.
     */
    fun markAllAsSeen() {
        viewModelScope.launch {
            val dealIds = notifiedDeals.value.map { it.deal_id }
            getNotificationsUseCase.markAllAsSeen(dealIds)
        }
    }

    /**
     * Factory for creating instances of [NotificationViewModel].
     */
    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val getNotificationsUseCase: GetNotificationsUseCase,
        private val sharedPrefManager: SharedPrefManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
                return NotificationViewModel(getNotificationsUseCase, sharedPrefManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}