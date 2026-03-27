package com.snaphubpro.zuvixapp.data.local

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.zuvix.snapvault.data.model.MediaType
import com.zuvix.snapvault.data.model.StatusItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "StatusScanner"
        
        // WhatsApp Status folder paths
        private const val WHATSAPP_STATUS_PATH = "Android/media/com.whatsapp/WhatsApp/.Statuses"
        private const val WHATSAPP_BUSINESS_STATUS_PATH = "Android/media/com.whatsapp.w4b/WhatsApp Business/.Statuses"
        
        // Legacy paths for older WhatsApp versions
        private const val LEGACY_WHATSAPP_STATUS_PATH = "WhatsApp/.Statuses"
        private const val LEGACY_BUSINESS_STATUS_PATH = "WhatsApp Business/.Statuses"
    }
    
    suspend fun scanStatuses(): List<StatusItem> = withContext(Dispatchers.IO) {
        val statusItems = mutableListOf<StatusItem>()
        
        // Get all possible status directories
        val statusDirs = getStatusDirectories()
        
        for (dir in statusDirs) {
            if (dir.exists() && dir.isDirectory) {
                val files = dir.listFiles() ?: continue
                
                files.filter { file ->
                    val ext = file.extension.lowercase()
                    ext in SUPPORTED_IMAGE_EXTENSIONS || ext in SUPPORTED_VIDEO_EXTENSIONS
                }.forEach { file ->
                    val type = if (file.extension.lowercase() in SUPPORTED_VIDEO_EXTENSIONS) {
                        MediaType.VIDEO
                    } else {
                        MediaType.IMAGE
                    }
                    
                    statusItems.add(
                        StatusItem(
                            id = file.absolutePath.hashCode().toString(),
                            uri = Uri.fromFile(file),
                            fileName = file.name,
                            type = type,
                            dateAdded = file.lastModified(),
                            size = file.length()
                        )
                    )
                }
            }
        }
        
        // Sort by date added (newest first)
        statusItems.sortedByDescending { it.dateAdded }
    }
    
    private fun getStatusDirectories(): List<File> {
        val dirs = mutableListOf<File>()
        
        // External storage paths
        val externalStorage = context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile?.parentFile
        
        externalStorage?.let { storage ->
            // New WhatsApp paths (Android 11+)
            dirs.add(File(storage, WHATSAPP_STATUS_PATH))
            dirs.add(File(storage, WHATSAPP_BUSINESS_STATUS_PATH))
            
            // Legacy paths
            dirs.add(File(storage, LEGACY_WHATSAPP_STATUS_PATH))
            dirs.add(File(storage, LEGACY_BUSINESS_STATUS_PATH))
        }
        
        return dirs
    }
    
    private val SUPPORTED_IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif")
    private val SUPPORTED_VIDEO_EXTENSIONS = setOf("mp4", "3gp", "mkv", "webm")
    
    suspend fun hasNewStatuses(lastScanTime: Long): Boolean = withContext(Dispatchers.IO) {
        val statuses = scanStatuses()
        statuses.any { it.dateAdded > lastScanTime }
    }
}
