package com.ycngmn.nobook.ui.components.sheet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ycngmn.nobook.R
import com.ycngmn.nobook.ui.NobookViewModel

@Composable
fun SheetContent(context: Activity, onRestart: () -> Unit, onClose: () -> Unit) {

    val isOpenDialog = remember { mutableStateOf(false) }

    val viewModel: NobookViewModel = viewModel(key = "Nobook")
    val removeAds = viewModel.removeAds.collectAsState()
    val pinchToZoom = viewModel.pinchToZoom.collectAsState()
    val enableDownloadContent = viewModel.enableDownloadContent.collectAsState()

    Box(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(state = rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp)
        ) {
            SheetItem(
                icon = R.drawable.ad_off_24px,
                title = stringResource(R.string.remove_ads_title),
                subtitle = stringResource(R.string.remove_ads_subtitle),
                isActive = removeAds.value
            ) {
                viewModel.setRemoveAds(!removeAds.value)
            }

            SheetItem(
                icon = R.drawable.download_24px,
                title = stringResource(R.string.download_content_title),
                subtitle = stringResource(R.string.download_content_subtitle),
                isActive = enableDownloadContent.value
            ) {
                viewModel.setEnableDownloadContent(!enableDownloadContent.value)
            }

            SheetItem(
                icon = R.drawable.pinch_zoom_out_24px,
                title = stringResource(R.string.pinch_to_zoom_title),
                subtitle = stringResource(R.string.pinch_to_zoom_subtitle),
                isActive = pinchToZoom.value
            ) {
                viewModel.setPinchToZoom(!pinchToZoom.value)
            }

            SheetItem(
                icon = R.drawable.widget_width_24px,
                title = stringResource(R.string.customize_feed_title),
                subtitle = stringResource(R.string.customize_feed_subtitle),
                iconColor = Color(0xFFD8A7B1)

            ) {
                isOpenDialog.value = true
            }

            if (isOpenDialog.value) HideOptionsDialog(viewModel) {
                isOpenDialog.value = false
            }

            if (!canOpenByDefault(context)) {

                SheetItem(
                    icon = R.drawable.open_in_browser_24px,
                    title = stringResource(R.string.open_in_nobook_title),
                    subtitle = stringResource(R.string.open_in_nobook_subtitle),
                    iconColor = Color(0xFF77E5B6)
                ) {
                    // Open open by default settings
                    val packageName = "package:${context.packageName}".toUri()
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, packageName)
                    else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageName)
                    context.startActivity(intent)
                }
            }

            SheetItem(
                icon = R.drawable.star_shine_24px,
                title = stringResource(R.string.star_at_github_title),
                subtitle = stringResource(R.string.star_at_github_subtitle),
                iconColor = Color(0XFFE6B800)
            ) {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/ycngmn/nobook".toUri())
                context.startActivity(intent)
            }

            Text(
                text = stringResource(R.string.changes_apply_next_session),
                fontSize = 16.sp,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            )

            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Card  (
                    modifier = Modifier.clickable { onRestart() },
                    shape = RoundedCornerShape(6.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        text = stringResource(R.string.apply_immediately),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(5.dp)
                    )
                }

                VerticalDivider(Modifier.height(30.dp),
                    color = Color.Gray, thickness = 2.dp)

                Card  (
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {

                    Text(
                        text = stringResource(R.string.close_menu),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(5.dp)
                            .clickable { onClose() }
                    )
                }
            }
        }
    }
}

@Composable
private fun HideOptionsDialog(viewModel: NobookViewModel, onClose: () -> Unit) {

    val hideSuggested = viewModel.hideSuggested.collectAsState()
    val hideReels = viewModel.hideReels.collectAsState()
    val hideStories = viewModel.hideStories.collectAsState()
    val hidePeopleYouMayKnow = viewModel.hidePeopleYouMayKnow.collectAsState()
    val hideGroups = viewModel.hideGroups.collectAsState()

    Dialog(
        onDismissRequest = { onClose() }
    ) {
        Card(
            shape = RoundedCornerShape(10.dp)
        ) {
            SheetItem(
                icon = R.drawable.public_off_24px,
                title = stringResource(R.string.hide_suggested_title),
                subtitle = stringResource(R.string.hide_suggested_subtitle),
                isActive = hideSuggested.value

            ) {
                viewModel.setHideSuggested(!hideSuggested.value)
            }

            SheetItem(
                icon = R.drawable.movie_off_24px,
                title = stringResource(R.string.hide_reels_title),
                subtitle = stringResource(R.string.hide_reels_subtitle),
                isActive = hideReels.value
            ) {
                viewModel.setHideReels(!hideReels.value)
            }

            SheetItem(
                icon = R.drawable.landscape_2_off_24px,
                title = stringResource(R.string.hide_stories_title),
                subtitle = stringResource(R.string.hide_stories_subtitle),
                isActive = hideStories.value
            ) {
                viewModel.setHideStories(!hideStories.value)
            }

            SheetItem(
                icon = R.drawable.frame_person_off_24px,
                title = stringResource(R.string.hide_people_you_may_know_title),
                subtitle = stringResource(R.string.hide_people_you_may_know_subtitle),
                isActive = hidePeopleYouMayKnow.value
            ) {
                viewModel.setHidePeopleYouMayKnow(!hidePeopleYouMayKnow.value)
            }

            SheetItem(
                icon = R.drawable.group_off_24px,
                title = stringResource(R.string.hide_groups_title),
                subtitle = stringResource(R.string.hide_groups_subtitle),
                isActive = hideGroups.value
            ) {
                viewModel.setHideGroups(!hideGroups.value)
            }
        }
    }
}

private fun canOpenByDefault(context: Context): Boolean {
    val urls = listOf(
        "https://www.facebook.com",
        "https://m.facebook.com",
        "https://fb.watch",
        "https://facebook.com"
    )

    val pm = context.packageManager
    val packageName = context.packageName

    for (url in urls) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolveInfo?.activityInfo?.packageName != packageName) {
            return false
        }
    }

    return true
}

