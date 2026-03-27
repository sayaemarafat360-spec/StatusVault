package com.zuvix.snapvault.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatusItem(
    val id: String,
    val uri: Uri,
    val fileName: String,
    val type: MediaType,
    val dateAdded: Long,
    val size: Long,
    val isVideo: Boolean = type == MediaType.VIDEO,
    val thumbnailUri: Uri? = null
) : Parcelable {
    
    val fileExtension: String
        get() = fileName.substringAfterLast(".", "")
    
    val displayName: String
        get() = fileName.substringBeforeLast(".")
}

enum class MediaType {
    IMAGE,
    VIDEO
}

@Parcelize
data class SavedStatus(
    val id: String,
    val originalUri: Uri,
    val savedUri: Uri,
    val fileName: String,
    val type: MediaType,
    val dateSaved: Long,
    val isFavorite: Boolean = false,
    val isVault: Boolean = false,
    val size: Long
) : Parcelable

@Parcelize
data class BulkSaveResult(
    val successCount: Int,
    val failedCount: Int,
    val totalItems: Int
) : Parcelable
