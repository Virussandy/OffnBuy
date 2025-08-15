package com.ozonic.offnbuy.repository

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.model.NotifiedDeal
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NotificationsRepository {

    private val dbRef = FirebaseDatabase.getInstance().getReference("Notifications")
    private val db = FirebaseFirestore.getInstance()

    suspend fun getInitialNotifications(): List<NotifiedDeal> = suspendCoroutine { continuation ->
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationList = mutableListOf<NotifiedDeal>()
                snapshot.children.forEach { childSnapshot ->
                    val notifiedDeal = childSnapshot.getValue(NotifiedDeal::class.java)
                    if (notifiedDeal != null) {
                        db.collection("deals").document(notifiedDeal.deal_id).get()
                            .addOnSuccessListener {
                                    dealSnapshot->
                                dealSnapshot.toObject(DealItem::class.java)?.let { deal ->
                                    notificationList.add(notifiedDeal.copy(deal = deal))
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
        onAdded: (NotifiedDeal) -> Unit,
        onChanged: (NotifiedDeal) -> Unit,
        onRemoved: (NotifiedDeal) -> Unit,  // dealId
        onMoved: (NotifiedDeal) -> Unit
    ) {
        dbRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val notifiedDeal = snapshot.getValue(NotifiedDeal::class.java) ?: return
                fetchDeal(notifiedDeal, onAdded)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val notifiedDeal = snapshot.getValue(NotifiedDeal::class.java) ?: return
                fetchDeal(notifiedDeal, onChanged)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val notifiedDeal = snapshot.getValue(NotifiedDeal::class.java) ?: return
                fetchDeal(notifiedDeal, onRemoved)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                val notifiedDeal = snapshot.getValue(NotifiedDeal::class.java) ?: return
                fetchDeal(notifiedDeal, onMoved)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationsRepository", "Database error: ${error.message}")
            }
        })
    }

    private fun fetchDeal(
        notifiedDeal: NotifiedDeal,
        callback: (NotifiedDeal) -> Unit
    ) {
        db.collection("deals").document(notifiedDeal.deal_id).get()
            .addOnSuccessListener { snap ->
                snap.toObject(DealItem::class.java)?.let { deal ->
                    callback(notifiedDeal.copy(deal = deal))
                }
            }
    }

}