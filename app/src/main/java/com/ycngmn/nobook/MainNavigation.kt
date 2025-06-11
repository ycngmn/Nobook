package com.ycngmn.nobook

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ycngmn.nobook.ui.screens.FacebookWebView
import com.ycngmn.nobook.ui.screens.MessengerWebView


@Composable
fun MainNavigation(data: Uri?) {

    val context = LocalContext.current
    val navController = rememberNavController()
    val shouldRestart = remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = "facebook") {
        // Adding column , fixes early initial load.. Idk why.
        composable("facebook") {
            key(shouldRestart.value) {
                Column {
                    FacebookWebView(
                        data?.toString() ?: "https://facebook.com/",
                        onRestart = { shouldRestart.value = !shouldRestart.value },
                        onOpenMessenger = {
                            Toast.makeText(context, "Opening messages...", Toast.LENGTH_SHORT).show()
                            navController.navigate("messenger")
                        }
                    )
                }
            }
        }
        composable("messenger") {
            Column {
                MessengerWebView(
                    onNavigateFB = { navController.popBackStack() }
                )
            }
        }
    }
}

