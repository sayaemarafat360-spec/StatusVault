package com.zuvix.snapvault.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.zuvix.snapvault.R
import java.io.File

fun Context.openWhatsApp(): Boolean {
    return try {
        val intent = packageManager.getLaunchIntentForPackage("com.whatsapp")
        if (intent != null) {
            startActivity(intent)
            true
        } else {
            Toast.makeText(this, getString(R.string.whatsapp_not_found), Toast.LENGTH_SHORT).show()
            false
        }
    } catch (e: Exception) {
        false
    }
}

fun Context.shareFile(uri: Uri, type: String) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val mimeType = if (type == "video") "video/mp4" else "image/jpeg"
        shareIntent.type = mimeType
        
        val chooserIntent = Intent.createChooser(shareIntent, "Share Status")
        startActivity(chooserIntent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.shareApp() {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out SnapVault - Status Saver! Save WhatsApp statuses easily. https://play.google.com/store/apps/details?id=$packageName")
        }
        startActivity(Intent.createChooser(shareIntent, "Share App"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.openPlayStore() {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        startActivity(intent)
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

fun Long.toTimeAgo(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        else -> "${diff / (24 * 60 * 60 * 1000)}d ago"
    }
}
