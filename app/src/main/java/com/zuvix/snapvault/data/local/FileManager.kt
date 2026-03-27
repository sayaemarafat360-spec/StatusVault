package com.zuvix.snapvault.data.local

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.zuvix.snapvault.data.model.MediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val APP_FOLDER_NAME = "SnapVault"
        const val IMAGES_FOLDER = "Images"
        const val VIDEOS_FOLDER = "Videos"
        const val VAULT_FOLDER = ".Vault"
        const val NOMEDIA_FILE = ".nomedia"
    }
    
    suspend fun saveStatus(
        sourceUri: Uri,
        type: MediaType,
        toVault: Boolean = false
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "status_${System.currentTimeMillis()}.${if (type == MediaType.VIDEO) "mp4" else "jpg"}"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveWithMediaStore(sourceUri, fileName, type, toVault)
            } else {
                saveLegacy(sourceUri, fileName, type, toVault)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun saveWithMediaStore(
        sourceUri: Uri,
        fileName: String,
        type: MediaType,
        toVault: Boolean
    ): Uri? {
        val collection = if (type == MediaType.VIDEO) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }
        
        val folderPath = if (toVault) {
            "${Environment.DIRECTORY_PICTURES}/$APP_FOLDER_NAME/$VAULT_FOLDER"
        } else {
            if (type == MediaType.VIDEO) {
                "${Environment.DIRECTORY_MOVIES}/$APP_FOLDER_NAME"
            } else {
                "${Environment.DIRECTORY_PICTURES}/$APP_FOLDER_NAME/$IMAGES_FOLDER"
            }
        }
        
        val contentValues = ContentValues().apply {
            put(
                if (type == MediaType.VIDEO) MediaStore.Video.Media.DISPLAY_NAME
                else MediaStore.Images.Media.DISPLAY_NAME,
                fileName
            )
            put(
                if (type == MediaType.VIDEO) MediaStore.Video.Media.RELATIVE_PATH
                else MediaStore.Images.Media.RELATIVE_PATH,
                folderPath
            )
            put(
                if (type == MediaType.VIDEO) MediaStore.Video.Media.MIME_TYPE
                else MediaStore.Images.Media.MIME_TYPE,
                if (type == MediaType.VIDEO) "video/mp4" else "image/jpeg"
            )
        }
        
        val contentResolver = context.contentResolver
        val newUri = contentResolver.insert(collection, contentValues) ?: return null
        
        contentResolver.openOutputStream(newUri)?.use { outputStream ->
            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        
        // Create .nomedia file for vault
        if (toVault) {
            createNoMediaFile(folderPath)
        }
        
        return newUri
    }
    
    private fun saveLegacy(
        sourceUri: Uri,
        fileName: String,
        type: MediaType,
        toVault: Boolean
    ): Uri? {
        val baseDir = File(
            Environment.getExternalStoragePublicDirectory(
                if (type == MediaType.VIDEO) Environment.DIRECTORY_MOVIES
                else Environment.DIRECTORY_PICTURES
            ),
            APP_FOLDER_NAME
        )
        
        val targetDir = if (toVault) {
            File(baseDir, VAULT_FOLDER).also { 
                it.mkdirs()
                File(it, NOMEDIA_FILE).createNewFile()
            }
        } else {
            File(baseDir, if (type == MediaType.VIDEO) VIDEOS_FOLDER else IMAGES_FOLDER).also {
                it.mkdirs()
            }
        }
        
        val targetFile = File(targetDir, fileName)
        
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        
        return Uri.fromFile(targetFile)
    }
    
    private fun createNoMediaFile(folderPath: String) {
        try {
            // For scoped storage, we need to create .nomedia via MediaStore
            val nomediaContent = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, NOMEDIA_FILE)
                put(MediaStore.Images.Media.RELATIVE_PATH, folderPath)
            }
            val contentResolver = context.contentResolver
            
            // Check if .nomedia exists
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(NOMEDIA_FILE)
            
            val cursor = contentResolver.query(
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                projection,
                selection,
                selectionArgs,
                null
            )
            
            if (cursor?.count == 0) {
                contentResolver.insert(
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    nomediaContent
                )
            }
            cursor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun deleteFile(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val rowsDeleted = contentResolver.delete(uri, null, null)
            rowsDeleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun moveToVault(uri: Uri, type: MediaType): Uri? = withContext(Dispatchers.IO) {
        try {
            // For simplicity, we'll copy to vault and delete original
            val newUri = saveStatus(uri, type, toVault = true)
            if (newUri != null) {
                deleteFile(uri)
            }
            newUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun getAppStoragePath(): File {
        return File(context.getExternalFilesDir(null), APP_FOLDER_NAME).also {
            it.mkdirs()
        }
    }
}
