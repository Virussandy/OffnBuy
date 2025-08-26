package com.ozonic.offnbuy.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent

/**
 * Finds the [Activity] from a given [Context].
 *
 * @return The [Activity] instance.
 * @throws IllegalStateException if the context is not an activity.
 */
fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

/**
 * Shares the application via a share intent.
 *
 * @param context The context from which to share.
 */
fun appShare(context: Context) {
    val shareText = "Check out OffnBuy for the best deals! Download it here:\n" +
            "https://play.google.com/store/apps/details?id=${context.packageName}"

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share OffnBuy via")
    context.startActivity(shareIntent)
}