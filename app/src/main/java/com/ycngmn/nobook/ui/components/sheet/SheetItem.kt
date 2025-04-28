package com.ycngmn.nobook.ui.components.sheet

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ycngmn.nobook.ui.theme.FacebookBlue

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
                modifier = Modifier.size(35.dp),
                colorFilter =
                    if (isActive)
                        ColorFilter.tint(FacebookBlue)
                    else ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
            )

            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 15.sp
                )
            }
        }
    }

    HorizontalDivider(Modifier, color = Color.Gray, thickness = 0.2.dp)
}
