package com.ycngmn.nobook.ui.components.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ycngmn.nobook.ui.theme.FacebookBlue

@Composable()
fun SheetItem(
    icon: Int,
    title: String,
    tailIcon: Int? = null,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.5.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 16.dp).weight(1F)
            )

            if (tailIcon == null) {

                Switch(
                    checked = isActive,
                    onCheckedChange = { onClick() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FacebookBlue,
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp),
                    thumbContent = if (isActive) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = FacebookBlue,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else null
                )
            }
            else {
                Icon(
                    painter = painterResource(tailIcon),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).padding(end = 18.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
