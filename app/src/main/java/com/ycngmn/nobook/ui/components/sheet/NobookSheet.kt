package com.ycngmn.nobook.ui.components.sheet

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.ycngmn.nobook.ui.NobookViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NobookSheet(
    viewModel: NobookViewModel,
    openBottomSheet: MutableState<Boolean>,
    onRestart: () -> Unit
) {

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val scope = rememberCoroutineScope()
    val closeSheet: () -> Unit = {
        scope.launch { bottomSheetState.hide() }
            .invokeOnCompletion {
                if (!bottomSheetState.isVisible) {
                    openBottomSheet.value = false
                }
            }
    }
    if (openBottomSheet.value) {

        ModalBottomSheet(
            modifier = Modifier.wrapContentHeight(),
            onDismissRequest = { openBottomSheet.value = false },
            sheetState = bottomSheetState,
        ) {
            SheetContent(viewModel,onRestart, closeSheet)
        }
    }
}


