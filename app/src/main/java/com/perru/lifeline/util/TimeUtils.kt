package com.perru.lifeline.util

import android.text.format.DateUtils

object TimeUtils {
    /**
     * Converts a millisecond timestamp into a relative time string like "Just now",
     * "2 hours ago", or "July 29".
     */
    fun toRelativeTime(millis: Long): String {
        val now = System.currentTimeMillis()
        if (now - millis < 60000) return "Just now"
        
        return DateUtils.getRelativeTimeSpanString(
            millis,
            now,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }
}
