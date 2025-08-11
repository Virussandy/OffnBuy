package com.ozonic.offnbuy.repository

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.model.NotificationItem
import com.ozonic.offnbuy.model.NotifiedDeal
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NotificationsRepository {

    private val dbRef = FirebaseDatabase.getInstance().getReference("Notifications")
    private val db = FirebaseFirestore.getInstance()

    suspend fun getInitialNotifications(): List<NotificationItem> = suspendCoroutine { continuation ->
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationList = mutableListOf<NotificationItem>()
                snapshot.children.forEach { childSnapshot ->
                    val notifiedDeal = childSnapshot.getValue(NotifiedDeal::class.java)
                    if (notifiedDeal != null) {
                        db.collection("deals").document(notifiedDeal.deal_id).get()
                            .addOnSuccessListener {
                                dealSnapshot->
                                dealSnapshot.toObject(DealItem::class.java)?.let { deal ->
                                    notificationList.add(NotificationItem(deal, notifiedDeal.timestamp))
                                    if(notificationList.size == snapshot.childrenCount.toInt()){
                                        continuation.resumeWith(Result.success(notificationList))
                                    }
                                }
                            }
                    }
                    if (snapshot.childrenCount == 0L){
                        continuation.resume(emptyList())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationsRepository", "Database error: ${error.message}")
                continuation.resume(emptyList())
            }
        })
    }

    fun listenForNewNotifications(
        onAdded: (NotificationItem) -> Unit,
        onChanged: (NotificationItem) -> Unit,
        onRemoved: (NotificationItem) -> Unit,  // dealId
        onMoved: (NotificationItem) -> Unit
    ) {
        dbRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val dealId = snapshot.getValue(NotifiedDeal::class.java)?.deal_id ?: return
                val timestamp = snapshot.getValue(NotifiedDeal::class.java)?.timestamp ?: return
                fetchDeal(dealId, timestamp, onAdded)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val dealId = snapshot.getValue(NotifiedDeal::class.java)?.deal_id ?: return
                val timestamp = snapshot.getValue(NotifiedDeal::class.java)?.timestamp ?: return
                fetchDeal(dealId, timestamp, onChanged)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val dealId = snapshot.getValue(NotifiedDeal::class.java)?.deal_id ?: return
                val timestamp = snapshot.getValue(NotifiedDeal::class.java)?.timestamp ?: return
                fetchDeal(dealId,timestamp, onRemoved)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                val dealId = snapshot.getValue(NotifiedDeal::class.java)?.deal_id ?: return
                val timestamp = snapshot.getValue(NotifiedDeal::class.java)?.timestamp ?: return
                fetchDeal(dealId, timestamp, onMoved)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationsRepository", "Database error: ${error.message}")
            }
        })
    }

    private fun fetchDeal(
        dealId: String,
        timestamp: Long,
        callback: (NotificationItem) -> Unit
    ) {
        db.collection("deals").document(dealId).get()
            .addOnSuccessListener { snap ->
                snap.toObject(DealItem::class.java)?.let { deal ->
                    callback(NotificationItem(deal, timestamp))
                }
            }
    }

}



