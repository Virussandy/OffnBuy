package com.ozonic.offnbuy.util

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.ozonic.offnbuy.data.AppDatabase

class  FirebaseInitialization : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        initiateFCM()
        createNotificationChannel(this)
    }

    fun initiateFCM(){
        FirebaseFirestore.getInstance()
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Successfully subscribed to 'all' topic")
                } else {
                    Log.e("FCM", "Failed to subscribe to topic", task.exception)
                }
            }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Good Deals"
            val descriptionText = "Get notified about best deals!"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("good_deals_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}