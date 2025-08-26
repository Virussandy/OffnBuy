package com.ozonic.offnbuy.util

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Converts a timestamp string to a human-readable "time ago" format.
 *
 * @param timestamp The timestamp string in ISO 8601 format.
 * @return A string representing the time elapsed since the timestamp.
 */
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