package com.perru.lifeline.presentation.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.perru.lifeline.ui.theme.Crimson
import com.perru.lifeline.ui.theme.SageGreen
import com.perru.lifeline.ui.theme.SageGreenLight
import com.perru.lifeline.ui.theme.Terracotta
import com.perru.lifeline.ui.theme.CrimsonLight
import com.perru.lifeline.util.OnboardingPrefs
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val accent: Color,
    val accentSoft: Color,
    val title: String,
    val subtitle: String
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Filled.Favorite,
        accent = Terracotta,
        accentSoft = Color(0xFFF3D9CE),
        title = "See who needs you, right now",
        subtitle = "Browse real-time blood requests from hospitals near you, filtered to your blood type."
    ),
    OnboardingPage(
        icon = Icons.Filled.MonitorHeart,
        accent = Crimson,
        accentSoft = CrimsonLight,
        title = "Stay donation-ready",
        subtitle = "A simple eligibility countdown keeps track of when you're next able to give."
    ),
    OnboardingPage(
        icon = Icons.Filled.Restaurant,
        accent = SageGreen,
        accentSoft = SageGreenLight,
        title = "A companion, not just a tracker",
        subtitle = "Nutrition guidance and recovery tips are on the way, built around keeping donors healthy."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    fun finish() {
        OnboardingPrefs.markOnboardingSeen(context)
        onFinished()
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 24.dp, bottom = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { finish() }) { Text("Skip") }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(page.accentSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(page.icon, contentDescription = null, tint = page.accent, modifier = Modifier.size(72.dp))
                }
                Spacer(Modifier.height(36.dp))
                Text(
                    page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    page.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            pages.indices.forEach { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 10.dp else 8.dp)
                        .background(
                            if (selected) pages[pagerState.currentPage].accent
                            else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
            Button(
                onClick = {
                    if (isLastPage) {
                        finish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = pages[pagerState.currentPage].accent),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(if (isLastPage) "Get started" else "Next")
            }
        }
    }
}
