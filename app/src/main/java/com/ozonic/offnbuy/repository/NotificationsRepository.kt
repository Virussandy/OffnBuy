package com.ozonic.offnbuy.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.model.NotifiedDeal
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NotificationsRepository {

    private val dbRef = FirebaseDatabase.getInstance().getReference("Notifications")
    private val db = FirebaseFirestore.getInstance()

    // In offnbuy/repository/NotificationsRepository.kt

    // This function now handles pagination.
    suspend fun getNotifications(pageSize: Int = 10, startKey: String? = null): Pair<List<NotifiedDeal>, String?> = suspendCoroutine { continuation ->
        // Query is ordered by key (which is chronological in Firebase RTDB)
        var query = dbRef.orderByKey().limitToLast(pageSize)

        // If startKey is provided, we fetch from that point backwards.
        if (startKey != null) {
            // We fetch one extra item to exclude it, as endAt is inclusive.
            query = dbRef.orderByKey().endAt(startKey).limitToLast(pageSize + 1)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationList = mutableListOf<NotifiedDeal>()
                val fullDealList = mutableListOf<Pair<NotifiedDeal, Task<DocumentSnapshot>>>()

                snapshot.children.forEach { child ->
                    child.getValue(NotifiedDeal::class.java)?.let { notifiedDeal ->
                        val dealTask = db.collection("deals").document(notifiedDeal.deal_id).get()
                        fullDealList.add(Pair(notifiedDeal, dealTask))
                    }
                }

                if (fullDealList.isEmpty()) {
                    continuation.resume(Pair(emptyList(), null))
                    return
                }

                // When all Firestore tasks are complete
                Tasks.whenAll(fullDealList.map { it.second }).addOnSuccessListener {
                    fullDealList.forEach { (notifiedDeal, task) ->
                        val deal = task.result?.toObject(DealItem::class.java)
                        if (deal != null) {
                            notificationList.add(notifiedDeal.copy(deal = deal))
                        }
                    }

                    // Reverse the list because limitToLast gets them in ascending order.
                    notificationList.reverse()

                    val lastKey = snapshot.children.firstOrNull()?.key

                    // If we fetched an extra item for pagination, remove it now.
                    if (startKey != null && notificationList.isNotEmpty()) {
                        notificationList.removeFirst()
                    }

                    continuation.resume(Pair(notificationList, lastKey))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(Pair(emptyList(), null))
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