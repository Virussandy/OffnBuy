package com.ozonic.offnbuy.util

import com.ozonic.offnbuy.repository.NotificationsRepository

// This class's only job is to start the background sync process.
class NotificationSyncManager(
    private val notificationsRepository: NotificationsRepository
) {
    fun start() {
        notificationsRepository.startListeningForNotifications()
    }
}