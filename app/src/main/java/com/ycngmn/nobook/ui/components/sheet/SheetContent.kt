package com.ycngmn.nobook.ui.components.sheet

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.ycngmn.nobook.R

@Preview(showBackground = true)
@Composable
fun SheetContent() {

    val context = LocalContext.current

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
            ) { }

            SheetItem(
                icon = R.drawable.public_off_24px,
                title = "Hide suggested posts",
                subtitle = "Hide posts from accounts you don't follow. May cause frequent loadings."

            ) { }

            SheetItem(
                icon = R.drawable.pinch_zoom_out_24px,
                title = "Pinch to zoom",
                subtitle = "Activate zoom in and out gestures.",
            ) { }

            SheetItem(
                icon = R.drawable.star_shine_24px,
                title = "Star at Github",
                subtitle = "Updates, bugs & contributions.",
            ) {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/ycngmn/nobook".toUri())
                context.startActivity(intent)
            }
        }
    }

}