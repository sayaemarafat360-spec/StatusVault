package com.zuvix.snapvault.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedStatusEntity::class,
        StatusViewHistory::class,
        NotificationPreferences::class,
        AppUsageStats::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SnapVaultDatabase : RoomDatabase() {
    abstract fun savedStatusDao(): SavedStatusDao
    abstract fun statusViewHistoryDao(): StatusViewHistoryDao
    abstract fun notificationPreferencesDao(): NotificationPreferencesDao
    abstract fun appUsageStatsDao(): AppUsageStatsDao
}
