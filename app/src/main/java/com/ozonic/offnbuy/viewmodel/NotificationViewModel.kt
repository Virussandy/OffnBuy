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
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.model.NotifiedDeal
import com.ozonic.offnbuy.repository.NotificationsRepository
import com.ozonic.offnbuy.util.SharedPrefManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class NotificationViewModel(
    application: Application               // need context -> extend AndroidViewModel
) : AndroidViewModel(application) {

    private val repository = NotificationsRepository()
    private val sharedPrefManager = SharedPrefManager(application)

    private val _notifiedDeals = MutableStateFlow<List<NotifiedDeal>>(emptyList())
    val notifiedDeals = _notifiedDeals.asStateFlow()

    private val alreadyNotified = mutableSetOf<String>()

    private val _unseenCount = MutableStateFlow(0)
    val unseenCount: StateFlow<Int> = _unseenCount.asStateFlow()



    init {
        viewModelScope.launch {
            if (sharedPrefManager.isFirstTimeRun()) {
                val initialNoification = repository.getInitialNotifications()
                val allDealIds = initialNoification.map { it.deal.deal_id }
                allDealIds.forEach { id ->
                    sharedPrefManager.addSeenDealId(id)
                }

                val dealsForUi = initialNoification.map {
                    NotifiedDeal(
                        deal = it.deal,
                        isSeen = true,
                        deal_id = it.deal.deal_id,
                        timestamp = it.timestamp
                    )
                }

                _notifiedDeals.value = dealsForUi.sortedByDescending { it.timestamp }
                _unseenCount.value = 0

                sharedPrefManager.setFirstTimeRun(false)
            } else {
                loadSeenDealIds()
            }
            attachRealtimeListeners()
        }
    }

    private fun attachRealtimeListeners(){
        repository.listenForNewNotifications(
            onAdded = { addedDeal ->
                val newDeal = addedDeal.deal
                val timestamp = addedDeal.timestamp

                if (_notifiedDeals.value.any { it.deal.deal_id == newDeal.deal_id }) return@listenForNewNotifications

                val isSeen = sharedPrefManager.getSeenDealsIds().contains(newDeal.deal_id)

                _notifiedDeals.update { current ->
                    (listOf(NotifiedDeal(deal = newDeal, isSeen = isSeen, newDeal.deal_id, timestamp = timestamp)) + current).sortedByDescending { it.timestamp }
                }

                _unseenCount.value = _notifiedDeals.value.count { !it.isSeen }

                if (!isSeen && alreadyNotified.add(newDeal.deal_id)) {
                    if (ActivityCompat.checkSelfPermission(
                            getApplication(),
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        showSystemNotification(getApplication(), newDeal)
                    }
                }
            },

            onChanged = { updatedDeal ->

                val newDeal = updatedDeal.deal
                val timestamp = updatedDeal.timestamp

                _notifiedDeals.update { list ->
                    list.map {
                        if (it.deal.deal_id == newDeal.deal_id) it.copy(deal = newDeal, timestamp = timestamp)
                        else it
                    }.sortedByDescending { it.timestamp }
                }
            },

            onRemoved = { removedDealId ->
                _notifiedDeals.update { list ->
                    list.filter { it.deal.deal_id != removedDealId.deal.deal_id }
                }
                alreadyNotified.remove(removedDealId.deal.deal_id)
                _unseenCount.value = _notifiedDeals.value.count { !it.isSeen }
            },

            onMoved = { movedDeal ->
                val newDeal = movedDeal.deal
                val timestamp = movedDeal.timestamp
                _notifiedDeals.update { list ->
                    val updatedList = list.filter { it.deal.deal_id != newDeal.deal_id }
                    val isSeen = sharedPrefManager.getSeenDealsIds().contains(newDeal.deal_id)
                    (listOf(NotifiedDeal(newDeal, isSeen, deal_id = newDeal.deal_id, timestamp = timestamp)) + updatedList).sortedByDescending { it.timestamp }
                }
            }
        )
    }

    private fun loadSeenDealIds(){
        val seenDealIds = sharedPrefManager.getSeenDealsIds()
        _notifiedDeals.update {
            list -> list.map {
                if (seenDealIds.contains(it.deal.deal_id)) it.copy(isSeen = true)
                else it
            }.sortedByDescending { it.timestamp }
        }
        _unseenCount.value = _notifiedDeals.value.count{ !it.isSeen}
    }


    fun markAsSeen(dealId: String){
        viewModelScope.launch {
            sharedPrefManager.addSeenDealId(dealId)
        }
        _notifiedDeals.update{list ->
            list.map{
                if (it.deal.deal_id == dealId) it.copy(isSeen = true)
                else it
            }.sortedByDescending { it.timestamp }
        }
        _unseenCount.value = _notifiedDeals.value.count { !it.isSeen }
    }

    fun markAllAsSeen() {
        viewModelScope.launch {
            val allDealIds = _notifiedDeals.value.map { it.deal.deal_id }
            allDealIds.forEach {
                it -> sharedPrefManager.addSeenDealId(it)
            }
        }

        _notifiedDeals.update { list ->
            list.map { it.copy(isSeen = true) }.sortedByDescending { it.timestamp }
        }

        _unseenCount.value = 0
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


