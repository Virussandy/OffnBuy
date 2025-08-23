package com.ozonic.offnbuy.viewmodel

import android.Manifest
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ozonic.offnbuy.MainActivity
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.data.AppDatabase
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.model.NotifiedDeal
import com.ozonic.offnbuy.model.NotifiedDealItem
import com.ozonic.offnbuy.repository.NotificationsRepository
import com.ozonic.offnbuy.util.SharedPrefManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


    class NotificationViewModel(application: Application) : AndroidViewModel(application) {

        private val notifiedDealDao = AppDatabase.getDatabase(application).notifiedDealDao()
        private val sharedPrefManager = SharedPrefManager(application)

        // The UI now gets its data from this single, reactive StateFlow.
        val notifiedDeals: StateFlow<List<NotifiedDeal>>

        // Unseen count is derived from the main list and seen IDs from SharedPreferences.
        val unseenCount: StateFlow<Int>

        init {
            // The main flow of all notifications from the local database.
            val allNotifiedDealsFlow = notifiedDealDao.getAllNotifications()
            val seenDealIdsFlow = sharedPrefManager.seenDealIdsFlow // A new flow for seen IDs

            // Combine the two flows. Whenever either the list of notifications or the set of
            // seen IDs changes, this will re-calculate the list and update the UI.
            notifiedDeals = combine(allNotifiedDealsFlow, seenDealIdsFlow) { deals, seenIds ->
                deals.map { deal ->
                    deal.copy(isSeen = seenIds.contains(deal.deal_id))
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

            // The unseen count is now also a reactive flow.
            unseenCount = notifiedDeals.map { list ->
                list.count { !it.isSeen }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        }

        // Marking as seen now simply means adding the ID to SharedPreferences.
        // The reactive flow will handle the UI update automatically.
        fun markAsSeen(dealId: String) {
            viewModelScope.launch {
                sharedPrefManager.addSeenDealId(dealId)
            }
        }

        fun markAllAsSeen() {
            viewModelScope.launch {
                val allDealIds = notifiedDeals.value.map { it.deal_id }
                sharedPrefManager.addSeenDealIds(allDealIds)
            }
        }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showSystemNotification(context: Context, deal: DealItem) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("deal_id", deal.deal_id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "good_deals_channel")
            .setSmallIcon(R.drawable.ic_app_icon) // your icon
            .setContentTitle("🔥 ${deal.title?.take(30)}")
            .setContentText("Now only ₹${deal.price} (${deal.discount})")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(deal.deal_id.hashCode(), builder.build())
    }
}