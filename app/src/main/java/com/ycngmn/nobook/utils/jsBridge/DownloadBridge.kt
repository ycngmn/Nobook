package com.ycngmn.nobook.utils.jsBridge

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.ycngmn.nobook.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class DownloadBridge(private val context: Context) {
    @JavascriptInterface
    fun downloadBase64File(base64Data: String, mimeType: String) {
        runCatching {
            if (!base64Data.contains(",")) {
                Toast.makeText(
                    context,
                    context.getString(R.string.download_failed_invalid_data),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val data = Base64.decode(base64Data.split(",")[1], Base64.DEFAULT)

            // Determine if it's an image or video
            val isImage = mimeType.startsWith("image/")
            val isVideo = mimeType.startsWith("video/")

            val (finalData, finalMimeType, extension) = when {
                isImage -> {
                    // Convert images to PNG for maximum compatibility
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    if (bitmap != null) {
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        Triple(outputStream.toByteArray(), "image/png", "png")
                    } else {
                        // If bitmap decoding fails, use original data
                        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
                        Triple(data, mimeType, ext)
                    }
                }
                isVideo -> {
                    // Keep videos as-is
                    val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "mp4"
                    Triple(data, mimeType, ext)
                }
                else -> {
                    // Unknown type, keep as-is
                    val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
                    Triple(data, mimeType, ext)
                }
            }

            val fileName = "${System.currentTimeMillis()}.$extension"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, finalMimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(finalData)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)

                    Toast.makeText(
                        context,
                        context.getString(R.string.saved_to_downloads),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)

                FileOutputStream(file).use { it.write(finalData) }
                Toast.makeText(
                    context,
                    context.getString(R.string.saved_to_downloads),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.onFailure {
            Toast.makeText(
                context,
                context.getString(R.string.failed_to_save_file),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}