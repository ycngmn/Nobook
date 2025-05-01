package com.ycngmn.nobook.ui.components.sheet

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ycngmn.nobook.R
import com.ycngmn.nobook.ui.NobookViewModel
import com.ycngmn.nobook.ui.theme.FacebookBlue

@Composable
fun SheetContent(context : Activity) {

    val viewModel: NobookViewModel = viewModel(key = "Nobook")
    val removeAds = viewModel.removeAds.collectAsState()
    val hideSuggested = viewModel.hideSuggested.collectAsState()
    val pinchToZoom = viewModel.pinchToZoom.collectAsState()

    Box(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
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
                subtitle = "Hide posts from accounts you don't follow. May cause frequent loadings.",
                isActive = hideSuggested.value

            ) {
                viewModel.setHideSuggested(!hideSuggested.value)
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
                icon = R.drawable.star_shine_24px,
                title = "Star at Github",
                subtitle = "Updates, bugs & contributions.",
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

            Text(
                text = "Apply Immediately?",
                fontSize = 16.sp,
                color = FacebookBlue,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(top = 5.dp, bottom = 16.dp).clickable {
                        // restart app
                        val intent = context.intent
                        context.finish()
                        context.startActivity(intent)
                    }
            )
        }
    }

}