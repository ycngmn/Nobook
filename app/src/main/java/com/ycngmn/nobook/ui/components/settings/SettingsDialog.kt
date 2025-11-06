package com.ycngmn.nobook.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
                    Modifier.background(themeColor.value)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        modifier = Modifier.align(Alignment.TopStart).size(22.dp),
                        onClick = { onDismiss() }
                    ) { Icon(Icons.AutoMirrored.Default.ArrowBackIos, "back button") }

                    Text(
                        "Nobook settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            },
            bottomBar = {
                Column {
                    HorizontalDivider(thickness = 1.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(themeColor.value)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { onReload() }) {
                            Text(
                                text = stringResource(R.string.apply_immediately),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        VerticalDivider(
                            Modifier.height(22.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8F),
                            thickness = 2.dp
                        )

                        TextButton(onClick = { onDismiss() }) {
                            Text(
                                text = stringResource(R.string.close_menu),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            SettingsContent(
                modifier = Modifier.padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                viewModel = viewModel
            )
        }

    }
}


