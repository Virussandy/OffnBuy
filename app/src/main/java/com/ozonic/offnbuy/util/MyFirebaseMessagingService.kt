package com.ozonic.offnbuy.util

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ozonic.offnbuy.AppState
import com.ozonic.offnbuy.MainActivity
import com.ozonic.offnbuy.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FirebaseMessaging.getInstance().subscribeToTopic("all")
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val dealId = remoteMessage.data["deal_id"] ?: return

        if (AppState.isInForeground) {
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("deal_id", dealId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, "good_deals_channel")
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(remoteMessage.notification?.title ?: "🔥 New Deal!")
            .setContentText(remoteMessage.notification?.body ?: "Tap to view")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)

        NotificationManagerCompat.from(this).notify(dealId.hashCode(), builder.build())
    }
}