package com.ozonic.offnbuy.util

import java.time.Instant
import java.time.temporal.ChronoUnit

fun getTimeAgo(timestamp: String?): String {
        if (timestamp == null) return ""

        return try {
            val instant = Instant.parse(timestamp)
            val now = Instant.now()
            val diff = ChronoUnit.MINUTES.between(instant, now)

            when {
                diff < 60 -> "$diff minutes ago"
                diff < 1440 -> "${diff / 60} hours ago"
                else -> "${diff / 1440} days ago"
            }
        } catch (e: Exception) {
            ""
        }
    }

