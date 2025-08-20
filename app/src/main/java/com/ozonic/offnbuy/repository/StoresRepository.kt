package com.ozonic.offnbuy.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ozonic.offnbuy.data.SupportedStoreDao
import com.ozonic.offnbuy.model.SupportedStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class StoresRepository(private val storeDao: SupportedStoreDao) {

    private val db = FirebaseFirestore.getInstance()

    // The UI will observe this flow, getting data instantly from the local DB.
    fun getSupportedStores(): Flow<List<SupportedStore>> {
        return storeDao.getAll()
    }

    // This function will be called to refresh the local data from Firestore.
    suspend fun syncSupportedStores() {
        try {
            val snapshot = db.collection("supportedStores").get().await()
            val firestoreStores = snapshot.toObjects(SupportedStore::class.java)
            if (firestoreStores.isNotEmpty()) {
                storeDao.clearAll()
                storeDao.insertAll(firestoreStores)
            }
        } catch (e: Exception) {
            // Handle error, e.g., log it. The app will continue
            // to work with the cached data.
        }
    }
}