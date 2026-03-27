package com.snaphubpro.zuvixapp.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.snaphubpro.zuvixapp.ads.AdManager
import com.zuvix.snapvault.data.local.FileManager
import com.zuvix.snapvault.data.local.StatusScanner
import com.zuvix.snapvault.data.local.VideoThumbnailExtractor
import com.zuvix.snapvault.data.local.database.*
import com.zuvix.snapvault.data.repository.StatusRepository
import com.zuvix.snapvault.service.notification.NotificationEngine
import com.zuvix.snapvault.util.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // ==================== DATABASE ====================
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SnapVaultDatabase {
        return Room.databaseBuilder(
            context,
            SnapVaultDatabase::class.java,
            "snapvault_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideSavedStatusDao(database: SnapVaultDatabase): SavedStatusDao {
        return database.savedStatusDao()
    }
    
    @Provides
    fun provideStatusViewHistoryDao(database: SnapVaultDatabase): StatusViewHistoryDao {
        return database.statusViewHistoryDao()
    }
    
    @Provides
    fun provideNotificationPreferencesDao(database: SnapVaultDatabase): NotificationPreferencesDao {
        return database.notificationPreferencesDao()
    }
    
    @Provides
    fun provideAppUsageStatsDao(database: SnapVaultDatabase): AppUsageStatsDao {
        return database.appUsageStatsDao()
    }
    
    // ==================== DATA SOURCES ====================
    
    @Provides
    @Singleton
    fun provideStatusScanner(@ApplicationContext context: Context): StatusScanner {
        return StatusScanner(context)
    }
    
    @Provides
    @Singleton
    fun provideFileManager(@ApplicationContext context: Context): FileManager {
        return FileManager(context)
    }
    
    @Provides
    @Singleton
    fun provideVideoThumbnailExtractor(@ApplicationContext context: Context): VideoThumbnailExtractor {
        return VideoThumbnailExtractor(context)
    }
    
    // ==================== REPOSITORY ====================
    
    @Provides
    @Singleton
    fun provideStatusRepository(
        statusScanner: StatusScanner,
        fileManager: FileManager,
        savedStatusDao: SavedStatusDao,
        statusViewHistoryDao: StatusViewHistoryDao,
        videoThumbnailExtractor: VideoThumbnailExtractor
    ): StatusRepository {
        return StatusRepository(
            statusScanner = statusScanner,
            fileManager = fileManager,
            savedStatusDao = savedStatusDao,
            statusViewHistoryDao = statusViewHistoryDao,
            videoThumbnailExtractor = videoThumbnailExtractor
        )
    }
    
    // ==================== SERVICES ====================
    
    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationEngine(@ApplicationContext context: Context): NotificationEngine {
        return NotificationEngine(context)
    }
    
    @Provides
    @Singleton
    fun provideAdManager(@ApplicationContext context: Context): AdManager {
        return AdManager(context)
    }
    
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
