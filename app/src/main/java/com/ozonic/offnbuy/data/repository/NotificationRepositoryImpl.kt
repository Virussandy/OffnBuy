package com.ozonic.offnbuy.data.repository

import android.app.Application
import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.ozonic.offnbuy.AppState
import com.ozonic.offnbuy.MainActivity
import com.ozonic.offnbuy.data.local.dao.NotifiedDealDao
import com.ozonic.offnbuy.data.local.model.NotifiedDealEntity
import com.ozonic.offnbuy.data.local.model.DealEntity
import com.ozonic.offnbuy.domain.model.NotifiedDeal
import com.ozonic.offnbuy.domain.repository.NotificationRepository
import com.ozonic.offnbuy.util.NetworkConnectivityObserver
import com.ozonic.offnbuy.util.SharedPrefManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Implementation of the [NotificationRepository] interface.
 * Handles fetching and managing notification data.
 */
class NotificationRepositoryImpl(
    private val application: Application,
    private val notifiedDealDao: NotifiedDealDao,
    private val connectivityObserver: NetworkConnectivityObserver,
    private val sharedPrefManager: SharedPrefManager
) : NotificationRepository {

    private val dbRef = FirebaseDatabase.getInstance().getReference("Notifications")
    private val firestore = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun getNotifiedDealsStream(): Flow<List<NotifiedDeal>> {
        return notifiedDealDao.getAllNotifications().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun startListeningForNotifications() {
        scope.launch {
            if (!connectivityObserver.observe().first()) {
                return@launch
            }

            dbRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val notifiedDeal = snapshot.getValue(NotifiedDealEntity::class.java) ?: return
                    fetchDealDetailsAndUpsert(notifiedDeal, isNew = true)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val notifiedDeal = snapshot.getValue(NotifiedDealEntity::class.java) ?: return
                    fetchDealDetailsAndUpsert(notifiedDeal, isNew = false)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val dealId = snapshot.key
                    if (dealId != null) {
                        scope.launch {
                            notifiedDealDao.delete(dealId)
                        }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { /* Not typically used */
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    override suspend fun markAsSeen(dealId: String) {
        sharedPrefManager.addSeenDealId(dealId)
    }

    override suspend fun markAllAsSeen(dealIds: List<String>) {
        sharedPrefManager.addSeenDealIds(dealIds)
    }

    private fun fetchDealDetailsAndUpsert(notifiedDeal: NotifiedDealEntity, isNew: Boolean) {
        firestore.collection("deals").document(notifiedDeal.deal_id).get()
            .addOnSuccessListener { document ->
                val dealItem = document.toObject(DealEntity::class.java)
                if (dealItem != null) {
                    val completeDeal = notifiedDeal.copy(deal = dealItem)
                    scope.launch {
                        notifiedDealDao.upsert(completeDeal)
                        if (isNew && !sharedPrefManager.getSeenDealsIds().contains(completeDeal.deal_id) && !AppState.isInForeground) {
                            showSystemNotification(application, dealItem)
                        }
                    }
                }
            }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showSystemNotification(context: Application, deal: DealEntity) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("deal_id", deal.deal_id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, deal.deal_id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "good_deals_channel")
            .setSmallIcon(com.ozonic.offnbuy.R.drawable.ic_app_icon)
            .setContentTitle("🔥 ${deal.title?.take(30)}")
            .setContentText("Now only ₹${deal.price} (${deal.discount})")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Use HIGH or MAX priority
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setFullScreenIntent(pendingIntent, true) // ✅ ADD THIS LINE to wake the screen

        NotificationManagerCompat.from(context).notify(deal.deal_id.hashCode(), builder.build())
    }
}