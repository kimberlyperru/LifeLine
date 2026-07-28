package com.perru.lifeline.util

import android.content.Context

/**
 * Tiny SharedPreferences wrapper tracking whether the person has already
 * seen the onboarding carousel, so it only shows once (before their first
 * sign-in), not on every app launch.
 */
object OnboardingPrefs {
    private const val PREFS_NAME = "lifeline_prefs"
    private const val KEY_ONBOARDING_SEEN = "onboarding_seen"

    fun hasSeenOnboarding(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_SEEN, false)

    fun markOnboardingSeen(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ONBOARDING_SEEN, true)
            .apply()
    }
}