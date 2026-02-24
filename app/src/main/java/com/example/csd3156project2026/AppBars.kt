package com.example.csd3156project2026

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csd3156project2026.ui.theme.CardBrown
import com.example.csd3156project2026.ui.theme.CreamText
import com.example.csd3156project2026.ui.theme.NavBrown
import com.example.csd3156project2026.ui.theme.WhiteText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String
) {
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
private fun BottomBarItem(
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