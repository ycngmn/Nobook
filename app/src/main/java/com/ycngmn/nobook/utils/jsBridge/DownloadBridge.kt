package com.ycngmn.nobook.utils.jsBridge

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class DownloadBridge(private val context: Context) {
    @JavascriptInterface
    fun downloadBase64File(base64Data: String, mimeType: String) {
        try {
            if (!base64Data.contains(",")) {
                Toast.makeText(context, "Failed to save file: Invalid data", Toast.LENGTH_LONG).show()
                return
            }

            val data = Base64.decode(base64Data.split(",")[1], Base64.DEFAULT)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
            val fileName = "${System.currentTimeMillis()}.$extension"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: Use MediaStore to save to public Downloads
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream: OutputStream ->
                        outputStream.write(data)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)

                    Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(context, "Failed to save file: Storage error", Toast.LENGTH_LONG).show()
                }
            } else {
                // Android 9 and below: Save to public Downloads with legacy storage
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)

                FileOutputStream(file).use {
                    it.write(data)
                }
                Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}