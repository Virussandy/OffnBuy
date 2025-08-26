package com.ozonic.offnbuy.data.repository

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.ozonic.offnbuy.data.local.dao.DealDao
import com.ozonic.offnbuy.data.local.model.DealEntity
import com.ozonic.offnbuy.domain.model.Deal
import com.ozonic.offnbuy.domain.repository.DealsRepository
import com.ozonic.offnbuy.util.NetworkConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class DealsRepositoryImpl(
    private val dealDao: DealDao,
    private val connectivityObserver: NetworkConnectivityObserver
) : DealsRepository {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val ioDispatcher = Dispatchers.IO

    override fun getDealsStream(): Flow<List<Deal>> {
        return dealDao.getDealsSteam().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun syncDeals(lastVisible: DocumentSnapshot?): DocumentSnapshot? = withContext(ioDispatcher) {
        if (!connectivityObserver.observe().first()) {
            return@withContext null
        }
        var query = db.collection("deals")
            .orderBy("posted_on", Query.Direction.DESCENDING)
            .limit(20)
        if (lastVisible != null) {
            query = query.startAfter(lastVisible)
        }
        val snapshot = query.get().await()
        val deals = snapshot.toObjects(DealEntity::class.java)
        if (deals.isNotEmpty()) {
            dealDao.insertAll(deals)
            dealDao.deleteOldestIfExceedsLimit()
        }
        return@withContext snapshot.documents.lastOrNull()
    }

    override suspend fun getDealFromFirestore(dealId: String): Deal? {
        if (!connectivityObserver.observe().first()) {
            return null
        }
        val entity = db.collection("deals").document(dealId).get().await().toObject(DealEntity::class.java)
        return entity?.toDomainModel()
    }

    override suspend fun syncNewestDeals() = withContext(ioDispatcher) {
        if (!connectivityObserver.observe().first()) {
            return@withContext
        }
        val query = db.collection("deals")
            .orderBy("posted_on", Query.Direction.DESCENDING)
            .limit(20)
        val snapshot = query.get().await()
        val deals = snapshot.toObjects(DealEntity::class.java)
        if (deals.isNotEmpty()) {
            dealDao.insertAll(deals)
            dealDao.deleteOldestIfExceedsLimit()
        }
    }

    override fun listenForNewDeals(latestDealTimestamp: String, onNewDeal: (Deal) -> Unit): ListenerRegistration? {
        // This check needs to be done in a coroutine context to access the flow's value
        // For simplicity, we assume we are online if we attempt to listen.
        // A more robust implementation might involve passing the connectivity status.
        val executor = Executors.newSingleThreadExecutor()
        return db.collection("deals")
            .orderBy("posted_on", Query.Direction.DESCENDING)
            .whereGreaterThan("posted_on", latestDealTimestamp)
            .limit(2)
            .addSnapshotListener(executor) { snapshot, _ ->
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val dealEntity = change.document.toObject(DealEntity::class.java)
                        onNewDeal(dealEntity.toDomainModel())
                    }
                }
            }
    }
}