package com.games.baghchal.engine

import android.content.Context
import android.content.SharedPreferences

object StatsManager {
    private const val PREF_NAME = "baghchal_stats"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var goatWins: Int
        get() = if (::prefs.isInitialized) prefs.getInt("goatWins", 0) else 0
        set(value) { if (::prefs.isInitialized) prefs.edit().putInt("goatWins", value).apply() }

    var tigerWins: Int
        get() = if (::prefs.isInitialized) prefs.getInt("tigerWins", 0) else 0
        set(value) { if (::prefs.isInitialized) prefs.edit().putInt("tigerWins", value).apply() }

    var currentStreak: Int
        get() = if (::prefs.isInitialized) prefs.getInt("currentStreak", 0) else 0
        set(value) {
            if (::prefs.isInitialized) {
                prefs.edit().putInt("currentStreak", value).apply()
                if (value > bestStreak) bestStreak = value
            }
        }

    var bestStreak: Int
        get() = if (::prefs.isInitialized) prefs.getInt("bestStreak", 0) else 0
        private set(value) { if (::prefs.isInitialized) prefs.edit().putInt("bestStreak", value).apply() }

    fun recordWin(playerSide: Turn) {
        if (playerSide == Turn.GOAT) {
            goatWins++
        } else {
            tigerWins++
        }
        currentStreak++
    }

    fun recordLoss() {
        currentStreak = 0
    }

    fun resetStats() {
        if (::prefs.isInitialized) {
            prefs.edit().clear().apply()
        }
    }
}
