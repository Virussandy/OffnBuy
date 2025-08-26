package com.ozonic.offnbuy.domain.usecase

import com.ozonic.offnbuy.domain.model.NotifiedDeal
import com.ozonic.offnbuy.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

/**
 * A use case for fetching and managing notifications.
 *
 * @param notificationRepository The repository for notifications.
 */
class GetNotificationsUseCase(private val notificationRepository: NotificationRepository) {

    /**
     * Returns a stream of all notified deals.
     *
     * @return A [Flow] of a list of [NotifiedDeal] objects.
     */
    fun execute(): Flow<List<NotifiedDeal>> {
        return notificationRepository.getNotifiedDealsStream()
    }

    /**
     * Marks a specific notification as seen.
     *
     * @param dealId The ID of the deal to mark as seen.
     */
    suspend fun markAsSeen(dealId: String) {
        notificationRepository.markAsSeen(dealId)
    }

    /**
     * Marks all notifications in the provided list as seen.
     *
     * @param dealIds The list of deal IDs to mark as seen.
     */
    suspend fun markAllAsSeen(dealIds: List<String>) {
        notificationRepository.markAllAsSeen(dealIds)
    }
}