package com.ozonic.offnbuy.util

import com.ozonic.offnbuy.domain.repository.NotificationRepository

/**
 * A utility class responsible for initiating the background process
 * that listens for real-time notifications from the data source.
 *
 * @param notificationRepository The repository that handles notification logic.
 */
class NotificationSyncManager(
    private val notificationRepository: NotificationRepository
) {
    /**
     * Starts the background listener for notifications.
     */
    fun start() {
        notificationRepository.startListeningForNotifications()
    }
}