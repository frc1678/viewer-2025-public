package org.citruscircuits.viewer.fragments.welcome

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.citruscircuits.viewer.MainViewerActivity.UserDataPoints
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WelcomePage(
    users: List<String>,
    onContinue: (selectedUser: String) -> Unit,
    onOpenMatchSchedule: () -> Unit,
) {
    Column(
        Modifier.padding(50.dp)
    ) {
        // Welcome Back Text
        Text(
            text = "Welcome back",
            textAlign = TextAlign.Start,
            modifier = Modifier.absolutePadding(top = 75.dp, bottom = 50.dp),
            fontSize = 40.sp,
            color = Color.DarkGray,
        )

        // Profile Selection Prompt
        Text(
            text = "Which profile would you like to use?",
            textAlign = TextAlign.Start,
            fontSize = 18.sp,
            color = Color.Gray,
            modifier = Modifier.absolutePadding(bottom = 20.dp)
        )

        // Retrieve last selected user or default to "OTHER"
        var selectedUser by remember {
            mutableStateOf(
                UserDataPoints.contents?.get("selected")?.asString?.uppercase(Locale.ROOT)
                    ?: "OTHER"
            )
        }


        Log.d("selectedUser", "selectedUser: $selectedUser")

        // Radio button selection group
        Column(Modifier.selectableGroup()) {
            users.forEach { text ->
                Row(
                    Modifier
                        .height(33.dp)
                        .selectable(
                            selected = text.uppercase(Locale.ROOT) == selectedUser,
                            onClick = { selectedUser = text.uppercase(Locale.ROOT) },
                            role = Role.RadioButton
                        )
                        .absolutePadding(top = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = text.uppercase() == selectedUser,
                        onClick = { selectedUser = text.uppercase(Locale.ROOT) },
                        colors = RadioButtonDefaults.colors(Color(0, 133, 119))
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }

            // Continue Button
            Button(
                onClick = {
                    onContinue(selectedUser.uppercase(Locale.ROOT)) // Save selected user
                    onOpenMatchSchedule() // Navigate to next screen
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0, 133, 119), contentColor = Color.White
                ),
                shape = RectangleShape
            ) {
                Text(
                    "CONTINUE", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
