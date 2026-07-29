package com.perru.lifeline.presentation.donor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.perru.lifeline.R
import com.perru.lifeline.presentation.common.RequestCard
import com.perru.lifeline.ui.theme.Crimson
import com.perru.lifeline.ui.theme.CreamSurface
import com.perru.lifeline.ui.theme.SageGreen
import com.perru.lifeline.ui.theme.SageGreenLight
import com.perru.lifeline.ui.theme.Terracotta
import com.perru.lifeline.util.BloodCompatibility
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DonorFeedScreen(
    viewModel: DonorViewModel = hiltViewModel(),
    onRequestClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val user = state.currentUser
    var showSwitchRoleDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    if (showSwitchRoleDialog) {
        SwitchRoleDialog(
            targetRoleLabel = "Hospital / Clinic",
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showSwitchRoleDialog = false
                user?.uid?.let { viewModel.switchRole(it) }
            },
            onDismiss = { showSwitchRoleDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
        ) {
            item {
                DonorHeader(
                    donorFirstName = user?.displayName?.takeIf { it.isNotBlank() }?.substringBefore(' '),
                    onSwitchRoleClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showSwitchRoleDialog = true 
                    }
                )
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(Modifier.height(16.dp))
                    EligibilityCard(lastDonationMillis = user?.lastDonationDateMillis)
                    Spacer(Modifier.height(14.dp))
                    NutritionHubCarousel()
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp)
                ) {
                    Text(
                        "Active requests",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    FilterChip(
                        selected = state.compatibleOnly,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleCompatibleOnly()
                        },
                        label = { Text(if (state.compatibleOnly) "Compatible with me" else "Showing all") }
                    )
                }
            }

            if (state.visibleRequests.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No active requests right now — check back soon.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(state.visibleRequests, key = { it.id }) { request ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 7.dp)) {
                        RequestCard(request = request, onClick = { onRequestClick(request.id) })
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

private data class NutritionTip(
    val title: String,
    val description: String,
    val tag: String,
    val imageUrl: String
)

private val nutritionTips = listOf(
    NutritionTip(
        title = "Iron + Vitamin C",
        description = "Pair iron-rich foods like spinach with citrus — it roughly triples how much iron your body absorbs.",
        tag = "Max Absorption",
        imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=800"
    ),
    NutritionTip(
        title = "Post-donation recovery",
        description = "Give your body 24–48 hours and plenty of water after donating before your next hard workout.",
        tag = "Rest & Hydrate",
        imageUrl = "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?q=80&w=800"
    ),
    NutritionTip(
        title = "Plant-based iron",
        description = "Beans, tofu, and fortified cereals are strong plant-based iron sources if you're vegetarian or vegan.",
        tag = "Vegan Options",
        imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=800"
    ),
    NutritionTip(
        title = "Kitchen tip",
        description = "Cooking in a cast-iron pan can meaningfully increase the iron content of acidic foods like tomato sauce.",
        tag = "Iron Boost",
        imageUrl = "https://images.unsplash.com/photo-1467003909585-2f8a72700288?q=80&w=800"
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NutritionHubCarousel() {
    val pagerState = rememberPagerState(pageCount = { nutritionTips.size })
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            if (!pagerState.isScrollInProgress) {
                val next = (pagerState.currentPage + 1) % nutritionTips.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Card(
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val tip = nutritionTips[pageIndex]
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = tip.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 100f
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Surface(
                        color = SageGreen,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            tip.tag,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White
                        )
                    }
                    Text(
                        tip.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        tip.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2
                    )
                }

                // Indicators
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    nutritionTips.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) Color.White
                                    else Color.White.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonorHeader(donorFirstName: String?, onSwitchRoleClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Terracotta, Crimson)),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        IconButton(onClick = onSwitchRoleClick, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Filled.SwapHoriz, contentDescription = "Switch account type", tint = CreamSurface)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(CreamSurface),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_lifeline),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "Hi${if (donorFirstName != null) ", $donorFirstName" else ""} 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CreamSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Thanks for being ready to save a life.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CreamSurface.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun SwitchRoleDialog(targetRoleLabel: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Switch to $targetRoleLabel?") },
        text = { Text("You'll be taken back through a quick setup step to switch your account type. Your existing profile details are kept.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Switch") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EligibilityCard(lastDonationMillis: Long?) {
    val windowDays = BloodCompatibility.ELIGIBILITY_WINDOW_DAYS
    val daysSince = lastDonationMillis?.let {
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it).toInt()
    }
    val daysRemaining = (daysSince?.let { windowDays - it } ?: 0).coerceIn(0, windowDays)
    val progress = if (lastDonationMillis == null) 1f else (daysSince!!.toFloat() / windowDays).coerceIn(0f, 1f)
    val eligible = lastDonationMillis == null || daysRemaining <= 0

    val backgroundBrush = if (eligible) {
        Brush.horizontalGradient(listOf(SageGreen, SageGreenLight))
    } else {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondaryContainer))
    }

    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().height(110.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                    CircularProgressIndicator(
                        progress = { progress },
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round,
                        color = if (eligible) Color.White else MaterialTheme.colorScheme.secondary,
                        trackColor = (if (eligible) Color.White else MaterialTheme.colorScheme.secondary).copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = if (eligible) "Ready" else "${daysRemaining}d",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (eligible) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        if (eligible) "You're eligible to donate!" else "Almost there",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (eligible) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (eligible) {
                            "Your body has fully replenished — ready to save a life?"
                        } else {
                            "$daysRemaining of $windowDays days left until your next eligible donation."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = (if (eligible) Color.White else MaterialTheme.colorScheme.onSecondaryContainer).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
