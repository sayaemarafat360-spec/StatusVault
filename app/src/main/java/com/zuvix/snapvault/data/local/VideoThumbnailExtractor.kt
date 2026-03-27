package com.snaphubpro.zuvixapp.data.local

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoThumbnailExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "VideoThumbnail"
        private const val THUMBNAIL_WIDTH = 480
        private const val THUMBNAIL_HEIGHT = 480
    }
    
    // Memory cache for thumbnails
    private val thumbnailCache = ConcurrentHashMap<String, Bitmap>()
    
    suspend fun extractThumbnail(videoUri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = videoUri.toString()
        
        // Check cache first
        thumbnailCache[cacheKey]?.let { return@withContext it }
        
        return@withContext try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoUri)
            
            // Get frame at 1 second or first available
            val bitmap = retriever.getFrameAtTime(
                1_000_000, // 1 second in microseconds
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            ) ?: retriever.frameAtTime
            
            retriever.release()
            
            // Scale down if needed
            val scaledBitmap = bitmap?.let { bmp ->
                if (bmp.width > THUMBNAIL_WIDTH || bmp.height > THUMBNAIL_HEIGHT) {
                    Bitmap.createScaledBitmap(bmp, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, true)
                        .also { bmp.recycle() }
                } else bmp
            }
            
            // Cache it
            scaledBitmap?.let { thumbnailCache[cacheKey] = it }
            
            scaledBitmap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract thumbnail: ${e.message}")
            null
        }
    }
    
    fun getCachedThumbnail(videoUri: Uri): Bitmap? {
        return thumbnailCache[videoUri.toString()]
    }
    
    fun clearCache() {
        thumbnailCache.values.forEach { it.recycle() }
        thumbnailCache.clear()
    }
}
