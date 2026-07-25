package com.ycngmn.nobook.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BrowserBottomBar(
    modifier: Modifier = Modifier,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface.copy(
                alpha = 0.78f,
            ),
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.22f,
                ),
            ),
            tonalElevation = 0.dp,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompactBrowserButton(
                    enabled = canGoBack,
                    onClick = onBack,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Go back",
                        modifier = Modifier.size(21.dp),
                    )
                }

                CompactBrowserButton(
                    onClick = onHome,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Facebook home",
                        modifier = Modifier.size(21.dp),
                    )
                }

                CompactBrowserButton(
                    onClick = onRefresh,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh page",
                        modifier = Modifier.size(21.dp),
                    )
                }

                CompactBrowserButton(
                    onClick = onSettings,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Nobook settings",
                        modifier = Modifier.size(21.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactBrowserButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp),
        content = content,
    )
}