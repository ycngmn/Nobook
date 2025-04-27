package com.ycngmn.nobook.ui.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.ycngmn.nobook.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NobookSheet(openBottomSheet: MutableState<Boolean>) {

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (openBottomSheet.value) {

        ModalBottomSheet(
            modifier = Modifier.wrapContentHeight(),
            onDismissRequest = { openBottomSheet.value = false },
            sheetState = bottomSheetState,
        ) {
            SheetContent()
        }
    }
}

@Composable()
fun SheetItem(
    icon: Int,
    title: String,
    subtitle: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clickable { onClick() },

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )

            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = subtitle,
                    color = Color.LightGray,
                    fontSize = 15.sp
                )
            }
        }
    }

    HorizontalDivider(Modifier, color = Color.Gray, thickness = 0.2.dp)
}

@Preview(showBackground = true)
@Composable
fun SheetContent() {

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .background(color = Color(0XFF232425))
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