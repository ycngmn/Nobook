package com.ycngmn.nobook.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.multiplatform.webview.web.LoadingState
import com.ycngmn.nobook.R
import com.ycngmn.nobook.ui.theme.GoogleDark

@Composable
fun SplashLoading(loadingState: LoadingState) {

    Column (
        modifier = Modifier.background(GoogleDark).fillMaxSize().zIndex(2F),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.logo_round),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(16.dp))

        Row (horizontalArrangement = Arrangement.SpaceBetween) {

                val progress = if (loadingState is LoadingState.Loading)
                    loadingState.progress else 0.8F

                repeat(5) {
                    Text(
                        text = "•",
                        fontSize = 40.sp,
                        color = if (progress >= it*0.20) Color.White else Color.Gray
                    )
                }
        }
    }
}