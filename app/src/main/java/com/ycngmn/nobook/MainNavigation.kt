package com.ycngmn.nobook

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ycngmn.nobook.ui.screens.FacebookWebView
import com.ycngmn.nobook.ui.screens.MessengerWebView


@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "facebook") {
        // Adding column , fixes early initial load.. Idk why.
        composable("facebook") {
            Column {
                FacebookWebView { navController.navigate("messenger") }
            }
        }
        composable("messenger") { Column { MessengerWebView() } }
    }
}

