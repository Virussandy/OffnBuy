package com.ozonic.offnbuy.util

import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.ozonic.offnbuy.domain.repository.UserDataRepository
import com.ozonic.offnbuy.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UserDataManager(
    private val scope: CoroutineScope,
    private val authViewModel: AuthViewModel,
    private val userDataRepository: UserDataRepository
) {
    private val activeListeners = mutableListOf<ListenerRegistration>()

    fun start() {
        scope.launch {
            authViewModel.currentUser
                .map { user -> user?.uid }
                .distinctUntilChanged()
                .collect { userId ->
                    Log.d("UserDataManager", "User session changed. New UID: $userId")
                    handleUserSessionChange(userId)
                }
        }
    }

    private suspend fun handleUserSessionChange(userId: String?) {
        stopAllListeners() // Always stop listeners on user change

        if (userId == null) {
            Log.d("UserDataManager", "User logged out. Clearing all local data.")
            userDataRepository.clearAllLocalUserData()
        } else {
            Log.d("UserDataManager", "Starting real-time listeners for user: $userId")
            try {
                val favoritesListener = userDataRepository.listenForFavoriteChanges(userId)
                val linksListener = userDataRepository.listenForGeneratedLinkChanges(userId)
                val profileListener = userDataRepository.listenForUserProfileChanges(userId)

                activeListeners.add(favoritesListener)
                activeListeners.add(linksListener)
                activeListeners.add(profileListener)
                Log.d("UserDataManager", "Real-time listeners started successfully.")
            } catch (e: Exception) {
                Log.e("UserDataManager", "Error starting listeners", e)
            }
        }
    }

    private fun stopAllListeners() {
        Log.d("UserDataManager", "Stopping ${activeListeners.size} active listeners.")
        activeListeners.forEach { it.remove() }
        activeListeners.clear()
    }
}