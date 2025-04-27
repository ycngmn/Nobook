package com.ycngmn.nobook.ui.components

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ycngmn.nobook.ui.theme.FacebookBlue


@Composable
fun NetworkErrorDialog(context: Context) {

    val activity = context as? Activity

    Dialog(
        onDismissRequest = {}
    ) {
        Column (
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)
        ) {
            Text(
                "Connect to a network",
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                "To use Nobook, turn on mobile data or connect to Wi-Fi.",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 20.dp),
                color = MaterialTheme.colorScheme.secondary
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.5.dp, FacebookBlue),
                shape = RoundedCornerShape(2.dp),
                onClick = { activity?.finish() }
            ) {
                Text(
                    "OK",
                    color = FacebookBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}