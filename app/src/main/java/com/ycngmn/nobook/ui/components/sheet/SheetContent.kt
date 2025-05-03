package com.ycngmn.nobook.ui.components.sheet

import android.app.Activity
import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ycngmn.nobook.R
import com.ycngmn.nobook.ui.NobookViewModel

@Composable
fun SheetContent(context: Activity, onRestart: () -> Unit, onClose: () -> Unit) {

    val viewModel: NobookViewModel = viewModel(key = "Nobook")
    val removeAds = viewModel.removeAds.collectAsState()
    val hideSuggested = viewModel.hideSuggested.collectAsState()
    val pinchToZoom = viewModel.pinchToZoom.collectAsState()
    val hideReels = viewModel.hideReels.collectAsState()
    val hideStories = viewModel.hideStories.collectAsState()
    val hidePeopleYouMayKnow = viewModel.hidePeopleYouMayKnow.collectAsState()

    Box(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(state = rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
        ) {
            SheetItem(
                icon = R.drawable.ad_off_24px,
                title = "Remove ads",
                subtitle = "Remove sponsored ads from feeds, videos and reels.",
                isActive = removeAds.value
            ) {
                viewModel.setRemoveAds(!removeAds.value)
            }

            SheetItem(
                icon = R.drawable.public_off_24px,
                title = "Hide suggested posts",
                subtitle = "May cause frequent loadings. Use \"Feeds\" from Menu instead.",
                isActive = hideSuggested.value

            ) {
                viewModel.setHideSuggested(!hideSuggested.value)
            }

            SheetItem(
                icon = R.drawable.movie_off_24px,
                title = "Hide reels",
                subtitle = "Hide reels from feed and videos.",
                isActive = hideReels.value
            ) {
                viewModel.setHideReels(!hideReels.value)
            }

            SheetItem(
                icon = R.drawable.landscape_2_off_24px,
                title = "Hide stories",
                subtitle = "Hide stories section from feed.",
                isActive = hideStories.value
            ) {
                viewModel.setHideStories(!hideStories.value)
            }

            SheetItem(
                icon = R.drawable.frame_person_off_24px,
                title = "Hide people you may know",
                subtitle = "Hides the section on search page.",
                isActive = hidePeopleYouMayKnow.value
            ) {
                viewModel.setHidePeopleYouMayKnow(!hidePeopleYouMayKnow.value)
            }

            SheetItem(
                icon = R.drawable.pinch_zoom_out_24px,
                title = "Pinch to zoom",
                subtitle = "Activate zoom in and out gestures.",
                isActive = pinchToZoom.value
            ) {
                viewModel.setPinchToZoom(!pinchToZoom.value)
            }

            SheetItem(
                icon = R.drawable.open_in_browser_24px,
                title = "Open in Nobook",
                subtitle = "Open facebook urls from other apps.",
                iconColor = Color(0xFF77E5B6)
            ) {
                // Open open by default settings
                val packageName = "package:${context.packageName}".toUri()
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, packageName)
                else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageName)
                context.startActivity(intent)
            }

            SheetItem(
                icon = R.drawable.star_shine_24px,
                title = "Star at Github",
                subtitle = "Updates, bugs & contributions.",
                iconColor = Color(0XFFE6B800)
            ) {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/ycngmn/nobook".toUri())
                context.startActivity(intent)
            }

            Text(
                text = "Changes will apply on your next session!",
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
                    shape = RoundedCornerShape(6.dp),
                    elevation = CardDefaults.cardElevation(7.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        text = "Apply Immediately?",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(5.dp)
                            .clickable {
                                onRestart()
                            }
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
                        text = "Close Menu",
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