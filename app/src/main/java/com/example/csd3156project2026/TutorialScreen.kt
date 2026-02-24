package com.example.csd3156project2026

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.csd3156project2026.ui.theme.ButtonBrown
import com.example.csd3156project2026.ui.theme.NavBrown
import com.example.csd3156project2026.ui.theme.WhiteText
import kotlinx.coroutines.launch

data class TutorialPage(
    val subtitle: String,
    val imageRes: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        TutorialPage(
            "Explore one cup at a time",
            R.drawable.tutorial_open
        ),
        TutorialPage(
            "See your current location and drop a pin to save a new coffee spot.",
            R.drawable.tutorial_1
        ),
        TutorialPage(
            "Rate your latte, cappuccino, or cold brew on the go.",
            R.drawable.tutorial_2
        ),
        TutorialPage(
            "View different ratings, all in one handy list.",
            R.drawable.tutorial_3
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box {
        Column(
            modifier = Modifier.fillMaxSize()
                               .navigationBarsPadding()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                TutorialPageContent(pages[page])
            }

            // progress bar
            @Suppress("DEPRECATION")
            LinearProgressIndicator(
                progress = (pagerState.currentPage + 1) / pages.size.toFloat(),
                color = NavBrown,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )

            Button(
                onClick = {
                    if (pagerState.currentPage == pages.lastIndex) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBrown,
                    contentColor = WhiteText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text =
                        if (pagerState.currentPage == pages.lastIndex)
                            "Begin your journey"
                        else
                            "Next"
                )
            }
        }
    }
}

@Composable
fun TutorialPageContent(page: TutorialPage) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(R.drawable.logo_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .scale(4.0f)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = page.subtitle,
                color = WhiteText,
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}