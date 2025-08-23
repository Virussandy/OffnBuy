package com.ozonic.offnbuy.util

import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.ozonic.offnbuy.repository.UserDataRepository
import com.ozonic.offnbuy.viewmodel.AuthState
import com.ozonic.offnbuy.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UserDataManager(
    private val scope: CoroutineScope,
    private val authViewModel: AuthViewModel,
    private val userDataRepository: UserDataRepository
) {
    // Keep track of active listeners to stop them later
    private val activeListeners = mutableListOf<ListenerRegistration>()

    fun start() {
        scope.launch {
            authViewModel.authState.map { (it as? AuthState.Authenticated)?.user?.uid }
                .distinctUntilChanged()
                .collect { userId ->
                    Log.d("UserDataManager", "User session changed. New UID: $userId")
                    // When the user changes, stop old listeners, clear local data,
                    // and start new listeners for the new user.
                    handleUserSessionChange(userId)
                }
        }
    }

    private suspend fun handleUserSessionChange(userId: String?) {
        stopAllListeners()
        userDataRepository.clearAllLocalUserData()

        if (userId != null) {
            Log.d("UserDataManager", "Starting real-time listeners for user: $userId")
            try {
                // Start all listeners, including the new profile listener
                val favoritesListener = userDataRepository.listenForFavoriteChanges(userId)
                val linksListener = userDataRepository.listenForGeneratedLinkChanges(userId)
                val profileListener = userDataRepository.listenForUserProfileChanges(userId) // ADD THIS

                activeListeners.add(favoritesListener)
                activeListeners.add(linksListener)
                activeListeners.add(profileListener) // ADD THIS
                Log.d("UserDataManager", "Real-time listeners started successfully.")
            } catch (e: Exception) {
                Log.e("UserDataManager", "Error starting listeners", e)
            }
        } else {
            Log.d("UserDataManager", "No authenticated user. Listeners will not be started.")
        }
    }

    private fun stopAllListeners() {
        Log.d("UserDataManager", "Stopping ${activeListeners.size} active listeners.")
        activeListeners.forEach { it.remove() }
        activeListeners.clear()
    }
}