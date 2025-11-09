package com.ycngmn.nobook.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.ycngmn.nobook.ui.theme.FacebookBlue

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val supportingText: String? = null,
    val isActive: Boolean? = null,
    val onClick: () -> Unit,
)

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    item: SettingsItem,
) {
    Row(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .clickable { item.onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = item.icon,
            tint = MaterialTheme.colorScheme.onSurface,
            contentDescription = null
        )

        Column(Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            item.supportingText?.let {
                Text(
                    text = item.supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (item.isActive != null) {
            Switch(
                checked = item.isActive,
                onCheckedChange = { item.onClick() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = FacebookBlue,
                ),
                modifier = Modifier,
                thumbContent = if (item.isActive) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = FacebookBlue,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else null
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = FacebookBlue
            )
        }
    }
}


@Composable
fun SettingsGroup(
    items: List<SettingsItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.fastForEachIndexed { index, item ->
            SettingsItem(
                item = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        when {
                            items.lastIndex == 0 -> {
                                RoundedCornerShape(28.dp)
                            }

                            index == 0 -> {
                                RoundedCornerShape(
                                    topStart = 28.dp,
                                    topEnd = 28.dp,
                                    bottomStart = 4.dp,
                                    bottomEnd = 4.dp
                                )
                            }

                            index == items.lastIndex -> {
                                RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp,
                                    bottomStart = 28.dp,
                                    bottomEnd = 28.dp
                                )
                            }

                            else -> {
                                RoundedCornerShape(4.dp)
                            }
                        }
                    )
            )
        }
    }
}