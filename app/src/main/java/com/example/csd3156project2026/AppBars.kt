package com.example.csd3156project2026

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csd3156project2026.ui.theme.CardBrown
import com.example.csd3156project2026.ui.theme.CreamText
import com.example.csd3156project2026.ui.theme.MainBrown
import com.example.csd3156project2026.ui.theme.NavBrown
import com.example.csd3156project2026.ui.theme.WhiteText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String
) {
    var showCredits by remember { mutableStateOf(false) }

    if (showCredits) {
        CreditsScreen(onDismiss = { showCredits = false })
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                color = WhiteText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp)
                    .clickable { showCredits = true }
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NavBrown,
            titleContentColor = WhiteText
        )
    )
}

@Composable
fun AppBottomBar(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onJournalClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    BottomAppBar(
        containerColor = NavBrown,
        contentColor = WhiteText
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomBarItem(
                icon = Icons.Filled.Home,
                label = "Home",
                selected = currentRoute == "home",
                onClick = onHomeClick
            )

            BottomBarItem(
                icon = Icons.Filled.List,
                label = "Reviews",
                selected = currentRoute == "journal",
                onClick = onJournalClick
            )

            BottomBarItem(
                icon = Icons.Filled.Person,
                label = "Profile",
                selected = currentRoute == "profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
fun BottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) CreamText else CardBrown

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = tint,
            fontSize = 12.sp
        )
    }
}

@Composable
fun CreditsScreen(onDismiss: () -> Unit) {

    Column(
        modifier = Modifier.fillMaxSize()
                           .padding(bottom = 40.dp)
                           .background(NavBrown)
                           .clickable { onDismiss() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text("Group 20 - Team Members:", fontSize = 20.sp, color = WhiteText)
        Spacer(modifier = Modifier.height(20.dp))

        Text("Lim Jun Kiat (2301484)", fontSize = 16.sp, color = WhiteText)
        Text("Kok Rui Huang (2301389)", fontSize = 16.sp, color = WhiteText)
        Text("Hermione See Kai Xin (2301494)", fontSize = 16.sp, color = WhiteText)
        Text("Yu Zhiwei Alvin (2301498)", fontSize = 16.sp, color = WhiteText)
        Text("Muhammad Hafiz Bin Onn (2301265)", fontSize = 16.sp, color = WhiteText)
        Text("Mohamed Arshad Bin Mohamed Hussain (2301364)", fontSize = 16.sp, color = WhiteText)

        Spacer(modifier = Modifier.height(20.dp))
        Text("Special Thanks", fontSize = 20.sp, color = WhiteText)
        Spacer(modifier = Modifier.height(20.dp))
        Text("Claud Comair", fontSize = 16.sp, color = WhiteText)
        Text("Dr. Kan Chen", fontSize = 16.sp, color = WhiteText)
        Text("Marcus Tan Kee Woon", fontSize = 16.sp, color = WhiteText)
        Text("Chen Ming", fontSize = 16.sp, color = WhiteText)

        Spacer(modifier = Modifier.height(20.dp))
        Text("© 2026 DigiPen", fontSize = 16.sp, color = WhiteText)
        Spacer(modifier = Modifier.height(20.dp))

        Text("Thank you for using our app!", fontSize = 20.sp, color = WhiteText)
        Text("Just one more cup! ☕", fontSize = 20.sp, color = WhiteText)
    }
}