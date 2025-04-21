package com.ycngmn.nobook.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.multiplatform.webview.web.AccompanistWebChromeClient
import com.multiplatform.webview.web.PlatformWebViewParams

// src: https://github.com/KevinnZou/compose-webview-multiplatform

@Composable
fun getPlatformWebViewParams(): PlatformWebViewParams? {
    var fileChooserIntent by remember { mutableStateOf<Intent?>(null) }

    val webViewChromeClient =
        remember {
            FileChoosableWebChromeClient(onShowFilePicker = { fileChooserIntent = it })
        }

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result: ActivityResult ->
            if (result.resultCode != Activity.RESULT_OK) {
//                Toast.makeText(
//                    webViewChromeClient.context,
//                    "resultCode is not RESULT_OK (value: ${result.resultCode})",
//                    Toast.LENGTH_SHORT,
//                ).show()
                webViewChromeClient.cancelFileChooser()
                return@rememberLauncherForActivityResult
            }

            val intent = result.data
            if (intent == null) {
//                Toast.makeText(
//                    webViewChromeClient.context,
//                    "result intent is null",
//                    Toast.LENGTH_SHORT,
//                ).show()
                webViewChromeClient.cancelFileChooser()
                return@rememberLauncherForActivityResult
            }

            val singleFile: Uri? = intent.data
            val multiFiles: List<Uri>? = intent.getUris()

            when {
                singleFile != null -> webViewChromeClient.onReceiveFiles(arrayOf(singleFile))
                multiFiles != null -> webViewChromeClient.onReceiveFiles(multiFiles.toTypedArray())
                else -> {
                    Toast.makeText(
                        webViewChromeClient.context,
                        "data and clipData is null",
                        Toast.LENGTH_SHORT,
                    ).show()
                    webViewChromeClient.cancelFileChooser()
                }
            }
        }

    LaunchedEffect(key1 = fileChooserIntent) {
        fileChooserIntent?.let {
            try {
                launcher.launch(fileChooserIntent!!)
            } catch (e: ActivityNotFoundException) {
                webViewChromeClient.cancelFileChooser()
            }
        }
    }

    return PlatformWebViewParams(chromeClient = webViewChromeClient)
}

private fun Intent.getUris(): List<Uri>? {
    val clipData = clipData ?: return null
    return (0 until clipData.itemCount).map { clipData.getItemAt(it).uri }
}

private class FileChoosableWebChromeClient(
    private val onShowFilePicker: (Intent) -> Unit,
) : AccompanistWebChromeClient() {
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?,
    ): Boolean {
        this.filePathCallback = filePathCallback
        val filePickerIntent = fileChooserParams?.createIntent()

        if (filePickerIntent == null) {
            cancelFileChooser()
        } else {
            onShowFilePicker(filePickerIntent)
        }
        return true
    }

    fun onReceiveFiles(uris: Array<Uri>) {
        filePathCallback?.onReceiveValue(uris)
        filePathCallback = null
    }

    fun cancelFileChooser() {
        filePathCallback?.onReceiveValue(null)
        filePathCallback = null
    }
}