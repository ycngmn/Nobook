package com.ycngmn.nobook.ui.components


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ycngmn.nobook.R
import com.ycngmn.nobook.ui.theme.GoogleDark

@Composable
fun PopLoadingAnimation(size: Int = 100) {

    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            scale.animateTo(
                0.8f,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                1f,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
        }
    }

    Box (
        modifier = Modifier.fillMaxSize().background(GoogleDark),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_round),
            contentDescription = "Loading logo",
            modifier = Modifier.scale(scale.value).size(size.dp)
        )
    }
}