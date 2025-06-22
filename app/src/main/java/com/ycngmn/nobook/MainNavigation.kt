package com.ycngmn.nobook

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ycngmn.nobook.ui.NobookViewModel
import com.ycngmn.nobook.ui.screens.FacebookWebView
import com.ycngmn.nobook.ui.screens.MessengerWebView
import com.ycngmn.nobook.ui.theme.NobookTheme

@Composable
fun MainNavigation(data: Uri?) {

    val context = LocalContext.current
    val viewModel: NobookViewModel = viewModel()
    val shouldRestart = remember { mutableStateOf(false) }
    val themeColor = viewModel.themeColor.value

    var screen by rememberSaveable { mutableStateOf("facebook") }

    NobookTheme(
        darkTheme = ColorUtils.calculateLuminance(themeColor.toArgb()) < 0.5
    ) {
        key(shouldRestart.value) {
            Navigation(screen, Modifier.fillMaxSize()) { currentScreen ->
                if (currentScreen == "facebook") {
                    FacebookWebView(
                        data?.toString() ?: "https://facebook.com/",
                        viewModel = viewModel,
                        onRestart = {
                            shouldRestart.value = !shouldRestart.value
                            viewModel.scripts.value = ""
                        },
                        onOpenMessenger = {
                            Toast.makeText(context, "Opening messages...", Toast.LENGTH_SHORT)
                                .show()
                            screen = "messages"
                        }
                    )
                } else {
                    MessengerWebView(
                        viewModel = viewModel,
                        onNavigateFB = { screen = "facebook" }
                    )
                }
            }
        }
    }
}

@Composable
private fun <T : Any> Navigation(
    currentScreen: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    Box(modifier) {
        Crossfade(targetState = currentScreen) { screen ->
            saveableStateHolder.SaveableStateProvider(screen) {
                content(screen)
            }
        }
    }
}