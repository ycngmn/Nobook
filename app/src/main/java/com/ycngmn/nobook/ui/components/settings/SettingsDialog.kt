package com.ycngmn.nobook.ui.components.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.ycngmn.nobook.NobookViewModel
import com.ycngmn.nobook.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    viewModel: NobookViewModel,
    onDismiss: () -> Unit,
    onReload: () -> Unit
) {

    val themeColor = viewModel.themeColor.collectAsState()
    val scrollState = rememberScrollState()

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
    ) {
        val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
        dialogWindowProvider?.window?.setDimAmount(0f)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Box(
                    Modifier
                        .background(themeColor.value)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        modifier = Modifier.align(Alignment.TopStart).size(22.dp),
                        onClick = { onDismiss() }
                    ) { Icon(Icons.AutoMirrored.Default.ArrowBackIos, "back button") }

                    Text(
                        stringResource(R.string.nobook_settings),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            },
            floatingActionButton = {
                val isVisible by remember {
                    derivedStateOf {
                        scrollState.maxValue == 0 || scrollState.value < scrollState.maxValue
                    }
                }
                AnimatedVisibility(
                    isVisible,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { onReload(); onDismiss() },
                        text = { Text(stringResource(R.string.apply_immediately)) },
                        icon = { Icon(Icons.Default.Bolt, null) }
                    )
                }
            }
        ) { paddingValues ->
            SettingsContent(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                viewModel = viewModel
            )
        }

    }
}


