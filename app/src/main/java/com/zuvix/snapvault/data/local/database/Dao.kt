package com.snaphubpro.zuvixapp.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedStatusDao {
    
    @Query("SELECT * FROM saved_statuses WHERE is_vault = 0 ORDER BY date_saved DESC")
    fun getAllSaved(): Flow<List<SavedStatusEntity>>
    
    @Query("SELECT * FROM saved_statuses WHERE is_favorite = 1 AND is_vault = 0 ORDER BY date_saved DESC")
    fun getAllFavorites(): Flow<List<SavedStatusEntity>>
    
    @Query("SELECT * FROM saved_statuses WHERE is_vault = 1 ORDER BY date_saved DESC")
    fun getAllVaultItems(): Flow<List<SavedStatusEntity>>
    
    @Query("SELECT * FROM saved_statuses WHERE id = :id")
    suspend fun getById(id: Long): SavedStatusEntity?
    
    @Query("SELECT * FROM saved_statuses WHERE saved_uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): SavedStatusEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(status: SavedStatusEntity): Long
    
    @Update
    suspend fun update(status: SavedStatusEntity)
    
    @Delete
    suspend fun delete(status: SavedStatusEntity)
    
    @Query("DELETE FROM saved_statuses WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("UPDATE saved_statuses SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
    
    @Query("UPDATE saved_statuses SET is_vault = 1 WHERE id = :id")
    suspend fun moveToVault(id: Long)
    
    @Query("SELECT COUNT(*) FROM saved_statuses WHERE is_vault = 0")
    suspend fun getSavedCount(): Int
    
    @Query("SELECT COUNT(*) FROM saved_statuses WHERE is_favorite = 1")
    suspend fun getFavoritesCount(): Int
    
    @Query("SELECT COUNT(*) FROM saved_statuses WHERE is_vault = 1")
    suspend fun getVaultCount(): Int
    
    @Query("DELETE FROM saved_statuses WHERE is_vault = 0")
    suspend fun clearAllSaved()
    
    @Query("DELETE FROM saved_statuses WHERE is_vault = 1")
    suspend fun clearVault()
}

@Dao
interface StatusViewHistoryDao {
    
    @Query("SELECT * FROM status_view_history ORDER BY viewed_at DESC LIMIT :limit")
    suspend fun getRecentHistory(limit: Int = 100): List<StatusViewHistory>
    
    @Query("SELECT * FROM status_view_history WHERE status_id = :statusId LIMIT 1")
    suspend fun getByStatusId(statusId: String): StatusViewHistory?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: StatusViewHistory)
    
    @Query("UPDATE status_view_history SET was_saved = 1 WHERE status_id = :statusId")
    suspend fun markAsSaved(statusId: String)
    
    @Query("SELECT COUNT(*) FROM status_view_history WHERE viewed_at > :since")
    suspend fun getViewedCountSince(since: Long): Int
    
    @Query("DELETE FROM status_view_history WHERE viewed_at < :before")
    suspend fun cleanOldRecords(before: Long)
}

@Dao
interface NotificationPreferencesDao {
    
    @Query("SELECT * FROM notification_preferences WHERE id = 1")
    suspend fun getPreferences(): NotificationPreferences?
    
    @Query("SELECT * FROM notification_preferences WHERE id = 1")
    fun getPreferencesFlow(): Flow<NotificationPreferences?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreferences(prefs: NotificationPreferences)
    
    @Query("UPDATE notification_preferences SET last_notification_time = :time WHERE id = 1")
    suspend fun updateLastNotificationTime(time: Long)
    
    @Query("UPDATE notification_preferences SET notification_count_today = notification_count_today + 1 WHERE id = 1")
    suspend fun incrementNotificationCount()
    
    @Query("UPDATE notification_preferences SET notification_count_today = 0, last_reset_date = :date WHERE id = 1")
    suspend fun resetDailyCount(date: String)
}

@Dao
interface AppUsageStatsDao {
    
    @Query("SELECT * FROM app_usage_stats WHERE id = 1")
    suspend fun getStats(): AppUsageStats?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setStats(stats: AppUsageStats)
    
    @Query("UPDATE app_usage_stats SET total_opens = total_opens + 1, last_open_date = :date WHERE id = 1")
    suspend fun incrementOpens(date: Long)
    
    @Query("UPDATE app_usage_stats SET total_statuses_saved = total_statuses_saved + 1 WHERE id = 1")
    suspend fun incrementSaved()
    
    @Query("UPDATE app_usage_stats SET most_active_hour = :hour WHERE id = 1")
    suspend fun updateMostActiveHour(hour: Int)
}
