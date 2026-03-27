package com.zuvix.snapvault.data.local.database

import android.net.Uri
import androidx.room.*
import com.zuvix.snapvault.data.model.MediaType

@Entity(tableName = "saved_statuses")
data class SavedStatusEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "original_uri")
    val originalUri: String,
    
    @ColumnInfo(name = "saved_uri")
    val savedUri: String,
    
    @ColumnInfo(name = "file_name")
    val fileName: String,
    
    @ColumnInfo(name = "type")
    val type: String,
    
    @ColumnInfo(name = "date_saved")
    val dateSaved: Long,
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "is_vault")
    val isVault: Boolean = false,
    
    @ColumnInfo(name = "size")
    val size: Long,
    
    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String? = null
) {
    fun toSavedStatus(): com.zuvix.snapvault.data.model.SavedStatus {
        return com.zuvix.snapvault.data.model.SavedStatus(
            id = id.toString(),
            originalUri = Uri.parse(originalUri),
            savedUri = Uri.parse(savedUri),
            fileName = fileName,
            type = if (type == "VIDEO") MediaType.VIDEO else MediaType.IMAGE,
            dateSaved = dateSaved,
            isFavorite = isFavorite,
            isVault = isVault,
            size = size
        )
    }
    
    companion object {
        fun fromSavedStatus(status: com.zuvix.snapvault.data.model.SavedStatus): SavedStatusEntity {
            return SavedStatusEntity(
                id = status.id.toLongOrNull() ?: 0,
                originalUri = status.originalUri.toString(),
                savedUri = status.savedUri.toString(),
                fileName = status.fileName,
                type = status.type.name,
                dateSaved = status.dateSaved,
                isFavorite = status.isFavorite,
                isVault = status.isVault,
                size = status.size
            )
        }
    }
}

@Entity(tableName = "status_view_history")
data class StatusViewHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "status_id")
    val statusId: String,
    
    @ColumnInfo(name = "viewed_at")
    val viewedAt: Long,
    
    @ColumnInfo(name = "was_saved")
    val wasSaved: Boolean = false
)

@Entity(tableName = "notification_preferences")
data class NotificationPreferences(
    @PrimaryKey
    val id: Int = 1,
    
    @ColumnInfo(name = "notifications_enabled")
    val notificationsEnabled: Boolean = true,
    
    @ColumnInfo(name = "check_interval_minutes")
    val checkIntervalMinutes: Int = 30,
    
    @ColumnInfo(name = "quiet_hours_start")
    val quietHoursStart: Int = 22, // 10 PM
    
    @ColumnInfo(name = "quiet_hours_end")
    val quietHoursEnd: Int = 8, // 8 AM
    
    @ColumnInfo(name = "last_notification_time")
    val lastNotificationTime: Long = 0,
    
    @ColumnInfo(name = "notification_count_today")
    val notificationCountToday: Int = 0,
    
    @ColumnInfo(name = "last_reset_date")
    val lastResetDate: String = "",
    
    @ColumnInfo(name = "user_typical_check_times")
    val userTypicalCheckTimes: String = "", // JSON array of hours
    
    @ColumnInfo(name = "smart_notifications_enabled")
    val smartNotificationsEnabled: Boolean = true
)

@Entity(tableName = "app_usage_stats")
data class AppUsageStats(
    @PrimaryKey
    val id: Int = 1,
    
    @ColumnInfo(name = "total_opens")
    val totalOpens: Int = 0,
    
    @ColumnInfo(name = "total_statuses_saved")
    val totalStatusesSaved: Int = 0,
    
    @ColumnInfo(name = "first_open_date")
    val firstOpenDate: Long = 0,
    
    @ColumnInfo(name = "last_open_date")
    val lastOpenDate: Long = 0,
    
    @ColumnInfo(name = "average_session_duration")
    val averageSessionDuration: Long = 0,
    
    @ColumnInfo(name = "most_active_hour")
    val mostActiveHour: Int = -1,
    
    @ColumnInfo(name = "hour_usage_distribution")
    val hourUsageDistribution: String = "" // JSON of hour -> count
)
