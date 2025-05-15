package org.citruscircuits.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**Refresh Popup is called on every screen to make sure
 * that the Finished Refreshing screen is shown and is used to check
 * which popup to show based on which values are true and false*/
@Composable
fun RefreshPopup() {
    // State to control the visibility of the popup
    val showPopup = ReloadingItems.showPopup
    // State to control the loading state
    val isLoading = ReloadingItems.isLoading
    // All of these are associated with the values in the ReloadingItems object
    if (showPopup.value) {
        PopupDialog(
            title = " Cancel Refresh ",
            onDismiss = { showPopup.value = false },
            isLoading = isLoading.value,
            loadingText = "Refreshing..."
        )
    } else if (ReloadingItems.stillLoading.value) {
        PopupDialog(
            title = " Still Refreshing ",
            onDismiss = { ReloadingItems.stillLoading.value = false },
            isLoading = isLoading.value,
            loadingText = "Refreshing..."
        )
    } else if (ReloadingItems.finished.value) {
        PopupDialog(
            title = " X ",
            onDismiss = { ReloadingItems.finished.value = false },
            isLoading = isLoading.value,
            finishedText = "Finished\nRefresh"
        )
    }
}

/** The Popup Dialog displays when the refresh button on the topappbar
 * is clicked. It creates the popup and the closing button for it*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopupDialog(
    title: String,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    loadingText: String = "",
    finishedText: String = ""
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp)),
    ) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .offset((-1.5).dp, 1.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0, 87, 75))
                    .clickable(onClick = onDismiss)
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
            )
            if (isLoading) {
                LoadingIndicator(loadingText)
            } else if (finishedText.isNotEmpty()) {
                Text(
                    text = finishedText,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(50.dp)
                )
            }
        }
    }
}

/**The Loading Indicator function displays a reloading circle with
 * "Reloading" text based on if the above conditions are true*/
@Composable
fun LoadingIndicator(loadingText: String) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(50.dp)
                .size(100.dp),
            strokeWidth = 15.dp,
            trackColor = Color(142, 200, 188),
            color = Color(0, 133, 119)
        )
        Text(
            text = loadingText,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}