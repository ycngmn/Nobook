package com.ycngmn.nobook.utils.jsBridge

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
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

    private fun getCustomFolderUri(): Uri? {
        val stored = context.getSharedPreferences("nobook_prefs", Context.MODE_PRIVATE)
            .getString("download_folder_uri", null) ?: return null
        return runCatching { Uri.parse(stored) }.getOrNull()
    }

    private fun saveToCustomFolder(
        folderUri: Uri,
        fileName: String,
        mimeType: String,
        data: ByteArray
    ): Boolean {
        return runCatching {
            val docId = DocumentsContract.getTreeDocumentId(folderUri)
            val dirUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, docId)
            val newFileUri = DocumentsContract.createDocument(
                context.contentResolver, dirUri, mimeType, fileName
            ) ?: return false
            context.contentResolver.openOutputStream(newFileUri)?.use { it.write(data) }
            true
        }.getOrDefault(false)
    }

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

            val isImage = mimeType.startsWith("image/")
            val isVideo = mimeType.startsWith("video/")

            val (finalData, finalMimeType, extension) = when {
                isImage -> {
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    if (bitmap != null) {
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        Triple(outputStream.toByteArray(), "image/png", "png")
                    } else {
                        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
                        Triple(data, mimeType, ext)
                    }
                }
                isVideo -> {
                    val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "mp4"
                    Triple(data, mimeType, ext)
                }
                else -> {
                    val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
                    Triple(data, mimeType, ext)
                }
            }

            val fileName = "${System.currentTimeMillis()}.$extension"

            val customFolder = getCustomFolderUri()
            if (customFolder != null && saveToCustomFolder(customFolder, fileName, finalMimeType, finalData)) {
                Toast.makeText(
                    context,
                    context.getString(R.string.saved_to_downloads),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

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
