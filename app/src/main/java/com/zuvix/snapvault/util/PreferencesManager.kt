package com.snaphubpro.zuvixapp.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "snapvault_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private val KEY_ONBOARDING_SHOWN = booleanPreferencesKey("onboarding_shown")
        private val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val KEY_VAULT_PIN = intPreferencesKey("vault_pin")
        private val KEY_VAULT_PIN_SET = booleanPreferencesKey("vault_pin_set")
        private val KEY_INTERSTITIAL_COUNT = intPreferencesKey("interstitial_count")
        private val KEY_LAST_INTERSTITIAL_TIME = longPreferencesKey("last_interstitial_time")
        private val KEY_INTERSTITIAL_DAILY_COUNT = intPreferencesKey("interstitial_daily_count")
        private val KEY_LAST_INTERSTITIAL_DATE = longPreferencesKey("last_interstitial_date")
        private val MAX_INTERSTITIAL_PER_DAY = 3
    }
    
    val onboardingShown: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_SHOWN] ?: false
    }
    
    val isPremium: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_PREMIUM] ?: false
    }
    
    val vaultPinSet: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VAULT_PIN_SET] ?: false
    }
    
    suspend fun setOnboardingShown(shown: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_SHOWN] = shown
        }
    }
    
    suspend fun setPremium(isPremium: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_PREMIUM] = isPremium
        }
    }
    
    suspend fun setVaultPin(pin: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VAULT_PIN] = pin
            prefs[KEY_VAULT_PIN_SET] = true
        }
    }
    
    suspend fun verifyVaultPin(pin: Int): Boolean {
        val storedPin = context.dataStore.data.map { prefs ->
            prefs[KEY_VAULT_PIN] ?: -1
        }
        return storedPin.first() == pin
    }
    
    suspend fun shouldShowInterstitial(): Boolean {
        val currentTime = System.currentTimeMillis()
        val currentDate = currentTime / (24 * 60 * 60 * 1000) // Days since epoch
        
        return context.dataStore.data.map { prefs ->
            val lastDate = prefs[KEY_LAST_INTERSTITIAL_DATE] ?: 0
            val dailyCount = if (lastDate == currentDate) {
                prefs[KEY_INTERSTITIAL_DAILY_COUNT] ?: 0
            } else {
                0
            }
            
            dailyCount < MAX_INTERSTITIAL_PER_DAY
        }.first()
    }
    
    suspend fun recordInterstitialShown() {
        val currentTime = System.currentTimeMillis()
        val currentDate = currentTime / (24 * 60 * 60 * 1000)
        
        context.dataStore.edit { prefs ->
            val lastDate = prefs[KEY_LAST_INTERSTITIAL_DATE] ?: 0
            val currentCount = if (lastDate == currentDate) {
                prefs[KEY_INTERSTITIAL_DAILY_COUNT] ?: 0
            } else {
                0
            }
            
            prefs[KEY_LAST_INTERSTITIAL_DATE] = currentDate
            prefs[KEY_INTERSTITIAL_DAILY_COUNT] = currentCount + 1
            prefs[KEY_LAST_INTERSTITIAL_TIME] = currentTime
        }
    }
}
