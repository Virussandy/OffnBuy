package com.ozonic.offnbuy.repository

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.ozonic.offnbuy.data.DealDao
import com.ozonic.offnbuy.model.DealItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class DealsRepository(val dealDao: DealDao) {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val ioDispatcher = Dispatchers.IO

    fun getDealsStream(): Flow<List<DealItem>>{
        return dealDao.getDealsSteam()
    }

    suspend fun syncDeals(lastVisible: DocumentSnapshot?): DocumentSnapshot? = withContext(ioDispatcher){
        var query = db.collection("deals")
            .orderBy("posted_on", Query.Direction.DESCENDING)
            .limit(20)

        if(lastVisible != null){
            query = query.startAfter(lastVisible)
        }

        val snapshot = query.get().await()
        val deals = snapshot.toObjects(DealItem::class.java)

        if(deals.isNotEmpty()){
            dealDao.insertAll(deals)
            dealDao.deleteOldestIfExceedsLimit()
        }

        snapshot.documents.lastOrNull()
    }

    suspend fun getDealFromFirestore(dealId: String): DealItem? {
        return db.collection("deals").document(dealId).get().await().toObject(DealItem::class.java)
    }

    suspend fun syncNewestDeals() = withContext(ioDispatcher){
        val query = db.collection("deals")
            .orderBy("posted_on", Query.Direction.DESCENDING)
            .limit(20)

        val snapshot = query.get().await()
        val deals = snapshot.toObjects(DealItem::class.java)

        if(deals.isNotEmpty()){
            dealDao.insertAll(deals)
            dealDao.deleteOldestIfExceedsLimit()
        }
    }
    fun listenForNewDeals(
        latestDealTimestamp: String, // consider using Timestamp instead of String (see note below)
        onNewDeal: (DealItem) -> Unit
    ): ListenerRegistration {
        // Run snapshot listener work off the main thread
        val executor = Executors.newSingleThreadExecutor()
        return db.collection("deals")
            .orderBy("posted_on", Query.Direction.DESCENDING)
            .whereGreaterThan("posted_on", latestDealTimestamp)
            .limit(2)
            .addSnapshotListener(executor) { snapshot, _ ->
                // Map to objects off main
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val deal = change.document.toObject(DealItem::class.java)
                        onNewDeal(deal) // Let the caller decide which thread to flip state on
                    }
                }
            }
    }
}