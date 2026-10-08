package com.games.baghchal.engine

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

object StatsManager {
    private const val PREF_NAME = "baghchal_stats"
    private lateinit var prefs: SharedPreferences

    var goatWins by mutableIntStateOf(0)
        private set

    var tigerWins by mutableIntStateOf(0)
        private set

    var currentStreak by mutableIntStateOf(0)
        private set

    var bestStreak by mutableIntStateOf(0)
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        goatWins = prefs.getInt("goatWins", 0)
        tigerWins = prefs.getInt("tigerWins", 0)
        currentStreak = prefs.getInt("currentStreak", 0)
        bestStreak = prefs.getInt("bestStreak", 0)
    }

    fun recordWin(playerSide: Turn) {
        if (playerSide == Turn.GOAT) {
            goatWins = goatWins + 1
            if (::prefs.isInitialized) prefs.edit().putInt("goatWins", goatWins).apply()
        } else {
            tigerWins = tigerWins + 1
            if (::prefs.isInitialized) prefs.edit().putInt("tigerWins", tigerWins).apply()
        }
        currentStreak = currentStreak + 1
        if (currentStreak > bestStreak) {
            bestStreak = currentStreak
            if (::prefs.isInitialized) prefs.edit().putInt("bestStreak", bestStreak).apply()
        }
        if (::prefs.isInitialized) prefs.edit().putInt("currentStreak", currentStreak).apply()
    }

    fun recordLoss() {
        currentStreak = 0
        if (::prefs.isInitialized) prefs.edit().putInt("currentStreak", 0).apply()
    }

    fun resetStats() {
        goatWins = 0
        tigerWins = 0
        currentStreak = 0
        bestStreak = 0
        if (::prefs.isInitialized) prefs.edit().clear().apply()
    }
}
