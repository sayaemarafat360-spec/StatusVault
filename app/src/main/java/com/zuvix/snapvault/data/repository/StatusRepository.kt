package com.snaphubpro.zuvixapp.data.repository

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.snaphubpro.zuvixapp.data.local.FileManager
import com.zuvix.snapvault.data.local.StatusScanner
import com.zuvix.snapvault.data.local.VideoThumbnailExtractor
import com.zuvix.snapvault.data.local.database.*
import com.zuvix.snapvault.data.model.MediaType
import com.zuvix.snapvault.data.model.SavedStatus
import com.zuvix.snapvault.data.model.StatusItem
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepository @Inject constructor(
    private val statusScanner: StatusScanner,
    private val fileManager: FileManager,
    private val savedStatusDao: SavedStatusDao,
    private val statusViewHistoryDao: StatusViewHistoryDao,
    private val videoThumbnailExtractor: VideoThumbnailExtractor
) {
    companion object {
        private const val TAG = "StatusRepository"
    }
    
    // In-memory cache for WhatsApp statuses (not saved yet)
    private val _statuses = MutableStateFlow<List<StatusItem>>(emptyList())
    val statuses: StateFlow<List<StatusItem>> = _statuses.asStateFlow()
    
    // Persisted saved items from database
    val savedStatuses: Flow<List<SavedStatus>> = savedStatusDao.getAllSaved()
        .map { entities -> entities.map { it.toSavedStatus() } }
    
    val favorites: Flow<List<SavedStatus>> = savedStatusDao.getAllFavorites()
        .map { entities -> entities.map { it.toSavedStatus() } }
    
    val vaultItems: Flow<List<SavedStatus>> = savedStatusDao.getAllVaultItems()
        .map { entities -> entities.map { it.toSavedStatus() } }
    
    // Track which statuses are "new" (not seen before)
    private val _newStatusIds = MutableStateFlow<Set<String>>(emptySet())
    val newStatusIds: StateFlow<Set<String>> = _newStatusIds.asStateFlow()
    
    private var lastScanTime: Long = 0
    
    /**
     * Load statuses from WhatsApp folder
     */
    suspend fun loadStatuses(): List<StatusItem> {
        val items = statusScanner.scanStatuses()
        
        // Detect new statuses
        val previousIds = _statuses.value.map { it.id }.toSet()
        val currentIds = items.map { it.id }.toSet()
        val newIds = currentIds - previousIds
        
        _newStatusIds.value = newIds
        _statuses.value = items
        lastScanTime = System.currentTimeMillis()
        
        // Record view history for new statuses
        items.forEach { status ->
            if (status.id in newIds) {
                statusViewHistoryDao.insert(
                    StatusViewHistory(
                        statusId = status.id,
                        viewedAt = System.currentTimeMillis(),
                        wasSaved = false
                    )
                )
            }
        }
        
        return items
    }
    
    /**
     * Refresh and detect new statuses
     */
    suspend fun refreshStatuses(): Pair<List<StatusItem>, Int> {
        val previousCount = _statuses.value.size
        val items = loadStatuses()
        val newCount = _newStatusIds.value.size
        return items to newCount
    }
    
    /**
     * Check if there are new statuses since last scan
     */
    suspend fun hasNewStatuses(): Boolean {
        return statusScanner.hasNewStatuses(lastScanTime)
    }
    
    /**
     * Get count of new statuses
     */
    fun getNewStatusCount(): Int = _newStatusIds.value.size
    
    /**
     * Mark a status as "seen" (clear new badge)
     */
    fun markAsSeen(statusId: String) {
        _newStatusIds.value = _newStatusIds.value - statusId
    }
    
    /**
     * Clear all new badges
     */
    fun clearAllNewBadges() {
        _newStatusIds.value = emptySet()
    }
    
    /**
     * Save a single status
     */
    suspend fun saveStatus(status: StatusItem, toVault: Boolean = false): Uri? {
        return try {
            val savedUri = fileManager.saveStatus(status.uri, status.type, toVault)
            
            if (savedUri != null) {
                // Extract video thumbnail if needed
                var thumbnail: Bitmap? = null
                if (status.type == MediaType.VIDEO) {
                    thumbnail = videoThumbnailExtractor.extractThumbnail(status.uri)
                }
                
                // Save to database
                val entity = SavedStatusEntity(
                    originalUri = status.uri.toString(),
                    savedUri = savedUri.toString(),
                    fileName = status.fileName,
                    type = status.type.name,
                    dateSaved = System.currentTimeMillis(),
                    isFavorite = false,
                    isVault = toVault,
                    size = status.size,
                    thumbnailPath = null // Could save thumbnail to file and store path
                )
                
                savedStatusDao.insert(entity)
                
                // Mark as saved in history
                statusViewHistoryDao.markAsSaved(status.id)
                
                // Clear new badge if present
                markAsSeen(status.id)
                
                Log.d(TAG, "Status saved successfully: ${status.fileName}")
            }
            
            savedUri
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save status: ${e.message}")
            null
        }
    }
    
    /**
     * Save multiple statuses
     */
    suspend fun saveMultipleStatuses(
        statuses: List<StatusItem>,
        toVault: Boolean = false
    ): Int {
        var successCount = 0
        statuses.forEach { status ->
            val result = saveStatus(status, toVault)
            if (result != null) successCount++
        }
        return successCount
    }
    
    /**
     * Toggle favorite status
     */
    suspend fun toggleFavorite(savedStatus: SavedStatus) {
        val id = savedStatus.id.toLongOrNull() ?: return
        savedStatusDao.updateFavorite(id, !savedStatus.isFavorite)
    }
    
    /**
     * Move item to vault
     */
    suspend fun moveToVault(savedStatus: SavedStatus): Boolean {
        return try {
            val id = savedStatus.id.toLongOrNull() ?: return false
            
            // Copy file to vault location
            val newUri = fileManager.moveToVault(savedStatus.savedUri, savedStatus.type)
            
            if (newUri != null) {
                // Update database
                savedStatusDao.moveToVault(id)
                
                Log.d(TAG, "Moved to vault: ${savedStatus.fileName}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to move to vault: ${e.message}")
            false
        }
    }
    
    /**
     * Delete a saved status
     */
    suspend fun deleteSavedStatus(savedStatus: SavedStatus): Boolean {
        return try {
            val id = savedStatus.id.toLongOrNull() ?: return false
            
            // Delete file
            val deleted = fileManager.deleteFile(savedStatus.savedUri)
            
            if (deleted) {
                // Delete from database
                savedStatusDao.deleteById(id)
                Log.d(TAG, "Deleted: ${savedStatus.fileName}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete: ${e.message}")
            false
        }
    }
    
    /**
     * Get status by ID
     */
    fun getStatusById(id: String): StatusItem? {
        return _statuses.value.find { it.id == id }
    }
    
    /**
     * Get saved status by ID from database
     */
    suspend fun getSavedStatusById(id: String): SavedStatus? {
        val idLong = id.toLongOrNull() ?: return null
        return savedStatusDao.getById(idLong)?.toSavedStatus()
    }
    
    /**
     * Get all statuses of specific type
     */
    fun getStatusesByType(type: MediaType): List<StatusItem> {
        return _statuses.value.filter { it.type == type }
    }
    
    /**
     * Get video thumbnail
     */
    suspend fun getVideoThumbnail(videoUri: Uri): Bitmap? {
        return videoThumbnailExtractor.extractThumbnail(videoUri)
    }
    
    /**
     * Check if a status is new
     */
    fun isNewStatus(statusId: String): Boolean {
        return statusId in _newStatusIds.value
    }
    
    /**
     * Restore saved items from device storage (for app reinstall scenarios)
     */
    suspend fun restoreSavedItems() {
        // This would scan the saved folder and re-populate the database
        // Implementation depends on how you want to handle app reinstalls
    }
    
    /**
     * Clean old history records
     */
    suspend fun cleanOldHistory() {
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        statusViewHistoryDao.cleanOldRecords(oneWeekAgo)
    }
    
    /**
     * Get statistics
     */
    suspend fun getStats(): Stats {
        return Stats(
            savedCount = savedStatusDao.getSavedCount(),
            favoritesCount = savedStatusDao.getFavoritesCount(),
            vaultCount = savedStatusDao.getVaultCount()
        )
    }
}

data class Stats(
    val savedCount: Int,
    val favoritesCount: Int,
    val vaultCount: Int
)
