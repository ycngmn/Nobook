package com.ycngmn.nobook.ui.components.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BurstMode
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diversity1
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.Padding
import androidx.compose.material.icons.filled.Try
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PanoramaWideAngle
import androidx.compose.material.icons.outlined.Pinch
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.ycngmn.nobook.NobookViewModel
import com.ycngmn.nobook.R
import com.ycngmn.nobook.ui.theme.FacebookBlue
import com.ycngmn.nobook.utils.isAutoDesktop

@Composable
fun SettingsContent(
    modifier: Modifier,
    viewModel: NobookViewModel
) {
    val context = LocalContext.current
    var isOpenDialog by rememberSaveable { mutableStateOf(false) }

    val removeAds = viewModel.removeAds.collectAsState()
    val enableDownloadContent = viewModel.enableDownloadContent.collectAsState()
    val desktopLayout = viewModel.desktopLayout.collectAsState()
    val immersiveMode = viewModel.immersiveMode.collectAsState()
    val stickyNavbar = viewModel.stickyNavbar.collectAsState()
    val pinchToZoom = viewModel.pinchToZoom.collectAsState()
    val amoledBlack = viewModel.amoledBlack.collectAsState()

    val isAutoDesktop = isAutoDesktop()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsGroup(
            items = listOf(
                SettingsItem(
                    icon = Icons.Outlined.Shield,
                    title = stringResource(R.string.remove_ads_title),
                    supportingText = stringResource(R.string.hide_sponsored_ads_from_your_feed),
                    isActive = removeAds.value,
                    onClick = { viewModel.setRemoveAds(!removeAds.value) },
                ),
                SettingsItem(
                    icon = Icons.Outlined.FileDownload,
                    title = stringResource(R.string.download_content_title),
                    supportingText = stringResource(R.string.enable_download_button_on_media_view),
                    isActive = enableDownloadContent.value,
                    onClick = { viewModel.setEnableDownloadContent(!enableDownloadContent.value) },
                ),
                SettingsItem(
                    icon = Icons.Outlined.GridView,
                    title = stringResource(R.string.customize_feed_title),
                    supportingText = stringResource(R.string.customize_feed),
                    isActive = null,
                    onClick = { isOpenDialog = true },
                )
            )
        )

        SettingsGroup(
            items = listOf(
                SettingsItem(
                    icon = Icons.Outlined.Pinch,
                    title = stringResource(R.string.pinch_to_zoom_title),
                    supportingText = stringResource(R.string.use_two_fingers_to_zoom_in_or_out),
                    isActive = pinchToZoom.value,
                    onClick = { viewModel.setPinchToZoom(!pinchToZoom.value) }
                ),
                SettingsItem(
                    icon = Icons.Outlined.DesktopWindows,
                    title = stringResource(R.string.desktop_layout_title),
                    supportingText = stringResource(R.string.force_desktop_layout_may_not_be_suitable_for_smaller_display),
                    isActive = desktopLayout.value,
                    onClick = { if (!isAutoDesktop) viewModel.setDesktopLayout(!desktopLayout.value) }
                ),
                SettingsItem(
                    icon = Icons.Outlined.PanoramaWideAngle,
                    title = stringResource(R.string.immersive_mode_title),
                    supportingText = stringResource(R.string.hide_system_bars_for_a_fullscreen_experience),
                    isActive = immersiveMode.value,
                    onClick = { viewModel.setImmersiveMode(!immersiveMode.value) }
                ),
                SettingsItem(
                    icon = Icons.Default.Padding,
                    title = stringResource(R.string.sticky_navbar_title),
                    supportingText = stringResource(R.string.keep_the_navigation_bar_visible_while_scrolling),
                    isActive = stickyNavbar.value,
                    onClick = { viewModel.setStickyNavbar(!stickyNavbar.value) }
                ),
                SettingsItem(
                    icon = Icons.Outlined.Circle,
                    title = stringResource(R.string.amoled_black_title),
                    supportingText = stringResource(R.string.enable_pure_black_theme_for_amoled_displays),
                    isActive = amoledBlack.value,
                    onClick = { viewModel.setAmoledBlack(!amoledBlack.value) }
                )
            )
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                modifier = Modifier.align(Alignment.Center),
                onClick = {
                    val githubRepoUrl = "https://github.com/ycngmn/nobook"
                    val intent = Intent(Intent.ACTION_VIEW, githubRepoUrl.toUri())
                    context.startActivity(intent)
                }
            ) {
                val packageName = context.packageName
                val versionName = context.packageManager.getPackageInfo(packageName, 0).versionName
                Text(
                    "$packageName | v$versionName",
                    modifier = Modifier.padding(4.dp),
                    maxLines = 1
                )
            }
        }

    }

    if(isOpenDialog) {
        HideOptionsDialog(
            viewModel = viewModel,
            onDismiss = { isOpenDialog = false }
        )
    }

}

@Composable
private fun HideOptionsDialog(
    viewModel: NobookViewModel,
    onDismiss: () -> Unit
) {

    val hideSuggested = viewModel.hideSuggested.collectAsState()
    val hideReels = viewModel.hideReels.collectAsState()
    val hideStories = viewModel.hideStories.collectAsState()
    val hidePeopleYouMayKnow = viewModel.hidePeopleYouMayKnow.collectAsState()
    val hideGroups = viewModel.hideGroups.collectAsState()

    Dialog(
        onDismissRequest = { onDismiss() }
    ) {

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            HideDialogItem(
                SettingsItem(
                    icon = Icons.Filled.Try,
                    title = stringResource(R.string.suggested_posts),
                    isActive = hideSuggested.value,
                    onClick = { viewModel.setHideSuggested(!hideSuggested.value) }
                )
            )

            HideDialogItem(
                SettingsItem(
                    icon = Icons.Filled.Camera,
                    title = stringResource(R.string.reels),
                    isActive = hideReels.value,
                    onClick = { viewModel.setHideReels(!hideReels.value) }
                )
            )

            HideDialogItem(
                SettingsItem(
                    icon = Icons.Filled.BurstMode,
                    title = stringResource(R.string.stories),
                    isActive = hideStories.value,
                    onClick = { viewModel.setHideStories(!hideStories.value) }
                )
            )

            if (!viewModel.desktopLayout.collectAsState().value) {
                HideDialogItem(
                    SettingsItem(
                        icon = Icons.Filled.EmojiPeople,
                        title = stringResource(R.string.people_you_may_know),
                        isActive = hidePeopleYouMayKnow.value,
                        onClick = { viewModel.setHidePeopleYouMayKnow(!hidePeopleYouMayKnow.value) }
                    )
                )

                HideDialogItem(
                    SettingsItem(
                        icon = Icons.Filled.Diversity1,
                        title = stringResource(R.string.groups),
                        isActive = hideGroups.value,
                        onClick = { viewModel.setHideGroups(!hideGroups.value) }
                    )
                )
            }
        }
    }

}

@Composable
private fun HideDialogItem(item: SettingsItem) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .clickable { item.onClick() }
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            item.icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(0.6F)
        )

        Text(
            text = item.title,
            color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
            fontSize = 18.sp,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1F)
        )

        item.isActive?.let {
            Switch(
                checked = item.isActive,
                onCheckedChange = { item.onClick() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White.copy(0.6F),
                    checkedTrackColor = FacebookBlue.copy(0.6F),
                ),
                modifier = Modifier,
                thumbContent = if (item.isActive) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = FacebookBlue.copy(0.6F),
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else null
            )
        }
    }
}