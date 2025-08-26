package com.ozonic.offnbuy.domain.usecase

import com.ozonic.offnbuy.domain.model.UserProfile
import com.ozonic.offnbuy.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow

/**
 * A use case for fetching and observing a user's profile.
 *
 * @param userDataRepository The repository for user-specific data.
 */
class GetUserProfileUseCase(private val userDataRepository: UserDataRepository) {

    /**
     * Returns a stream of the current user's profile.
     *
     * @param userId The ID of the user.
     * @return A [Flow] of a [UserProfile] object, or null if not found.
     */
    fun execute(userId: String): Flow<UserProfile?> {
        return userDataRepository.getUserProfile(userId)
    }
}