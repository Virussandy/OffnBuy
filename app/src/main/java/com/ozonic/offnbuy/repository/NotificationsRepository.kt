package com.ozonic.offnbuy.repository

import android.Manifest
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.ozonic.offnbuy.MainActivity
import com.ozonic.offnbuy.R
import com.ozonic.offnbuy.data.NotifiedDealDao
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.model.NotifiedDeal
import com.ozonic.offnbuy.util.NetworkConnectivityObserver
import com.ozonic.offnbuy.util.SharedPrefManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Inject Application context for showing notifications
class NotificationsRepository(
    private val application: Application,
    private val notifiedDealDao: NotifiedDealDao,
    private val connectivityObserver: NetworkConnectivityObserver,
    private val sharedPrefManager: SharedPrefManager
) {

    private val dbRef = FirebaseDatabase.getInstance().getReference("Notifications")
    private val firestore = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startListeningForNotifications() {
        scope.launch {
            if (!connectivityObserver.observe().first()) {
                Log.d("NotificationsRepository", "Offline. Skipping listener attachment.")
                return@launch
            }

            dbRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val notifiedDeal = snapshot.getValue(NotifiedDeal::class.java) ?: return
                    fetchDealDetailsAndUpsert(notifiedDeal, isNew = true)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val notifiedDeal = snapshot.getValue(NotifiedDeal::class.java) ?: return
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

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { /* Not typically used */ }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotificationsRepository", "Database error: ${error.message}")
                }
            })
        }
    }

    private fun fetchDealDetailsAndUpsert(notifiedDeal: NotifiedDeal, isNew: Boolean) {
        firestore.collection("deals").document(notifiedDeal.deal_id).get()
            .addOnSuccessListener { document ->
                val dealItem = document.toObject(DealItem::class.java)
                if (dealItem != null) {
                    val completeDeal = notifiedDeal.copy(deal = dealItem)
                    scope.launch {
                        notifiedDealDao.upsert(completeDeal)
                        // If it's a newly added deal and the user hasn't seen it, show a notification
                        if (isNew && !sharedPrefManager.getSeenDealsIds().contains(completeDeal.deal_id)) {
                            showSystemNotification(application, dealItem)
                        }
                    }
                }
            }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showSystemNotification(context: Application, deal: DealItem) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("deal_id", deal.deal_id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, deal.deal_id.hashCode(), intent, // Use a unique request code
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "good_deals_channel")
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle("🔥 ${deal.title?.take(30)}")
            .setContentText("Now only ₹${deal.price} (${deal.discount})")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(deal.deal_id.hashCode(), builder.build())
    }
}