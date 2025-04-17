package com.ycngmn.nobook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.ycngmn.nobook.ui.screens.FacebookWebView

import com.ycngmn.nobook.ui.theme.NobookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NobookTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(Modifier.padding(innerPadding)) {
                        FacebookWebView()
                    }
                }
            }
        }
    }
}