package com.perru.lifeline.presentation.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.perru.lifeline.ui.theme.Crimson
import com.perru.lifeline.ui.theme.SageGreen
import com.perru.lifeline.ui.theme.SageGreenLight
import com.perru.lifeline.ui.theme.Terracotta
import com.perru.lifeline.ui.theme.CrimsonLight
import com.perru.lifeline.util.OnboardingPrefs
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

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
    val haptic = LocalHapticFeedback.current
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    fun finish() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
            val pageOffset = (
                (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
            ).absoluteValue

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .graphicsLayer {
                        // Fade and scale title/subtitle based on offset
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer {
                            // Fancy scaling and rotating effect for the icon background
                            val scale = lerp(
                                start = 0.8f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                            scaleX = scale
                            scaleY = scale
                            rotationZ = lerp(
                                start = -30f,
                                stop = 0f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        }
                        .background(page.accentSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        page.icon,
                        contentDescription = null,
                        tint = page.accent,
                        modifier = Modifier.size(72.dp)
                    )
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

        // Expanding Pill Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            pages.indices.forEach { index ->
                val selected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (selected) 24.dp else 8.dp,
                    label = "width"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(width)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (selected) pages[pagerState.currentPage].accent
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
