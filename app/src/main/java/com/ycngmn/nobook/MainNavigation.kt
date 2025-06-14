package com.ycngmn.nobook

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ycngmn.nobook.ui.NobookViewModel
import com.ycngmn.nobook.ui.screens.FacebookWebView
import com.ycngmn.nobook.ui.screens.MessengerWebView


@Composable
fun MainNavigation(data: Uri?) {

    val context = LocalContext.current
    val navController = rememberNavController()
    val viewModel: NobookViewModel = viewModel()
    val shouldRestart = remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = "facebook") {
        // Adding column , fixes early initial load.. Idk why.
        composable("facebook") {
            key(shouldRestart.value) {

                FacebookWebView(
                    data?.toString() ?: "https://facebook.com/",
                    viewModel = viewModel,
                    onRestart = {
                        shouldRestart.value = !shouldRestart.value
                        viewModel.setScripts("")
                    },
                    onOpenMessenger = {
                        Toast.makeText(context, "Opening messages...", Toast.LENGTH_SHORT).show()
                        navController.navigate("messenger")
                    }
                )

            }
        }
        composable("messenger") {

            MessengerWebView(
                viewModel = viewModel,
                onNavigateFB = { navController.popBackStack() }
            )

        }
    }
}

