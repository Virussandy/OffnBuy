package com.ozonic.offnbuy.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ozonic.offnbuy.data.local.dao.SupportedStoreDao
import com.ozonic.offnbuy.data.local.model.SupportedStoreEntity
import com.ozonic.offnbuy.domain.model.SupportedStore
import com.ozonic.offnbuy.domain.repository.SupportedStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SupportedStoreRepositoryImpl(
    private val supportedStoreDao: SupportedStoreDao
) : SupportedStoreRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun getStores(): Flow<List<SupportedStore>> {
        return supportedStoreDao.getAll().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun syncStores() {
        withContext(Dispatchers.IO) {
            try {
                val snapshot = db.collection("supportedStores").get().await()
                val stores = snapshot.toObjects(SupportedStoreEntity::class.java)
                if (stores.isNotEmpty()) {
                    supportedStoreDao.insertAll(stores)
                }
            } catch (e: Exception) {
                // Handle exceptions, e.g., logging
                e.printStackTrace()
            }
        }
    }
}