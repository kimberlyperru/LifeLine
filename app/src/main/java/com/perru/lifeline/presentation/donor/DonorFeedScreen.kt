package com.perru.lifeline.presentation.donor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.perru.lifeline.R
import com.perru.lifeline.presentation.common.RequestCard
import com.perru.lifeline.ui.theme.Crimson
import com.perru.lifeline.ui.theme.CreamSurface
import com.perru.lifeline.ui.theme.SageGreen
import com.perru.lifeline.ui.theme.SageGreenLight
import com.perru.lifeline.ui.theme.Terracotta
import com.perru.lifeline.util.BloodCompatibility
import java.util.concurrent.TimeUnit

@Composable
fun DonorFeedScreen(
    viewModel: DonorViewModel = hiltViewModel(),
    onRequestClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val user = state.currentUser
    var showSwitchRoleDialog by remember { mutableStateOf(false) }

    if (showSwitchRoleDialog) {
        SwitchRoleDialog(
            targetRoleLabel = "Hospital / Clinic",
            onConfirm = {
                showSwitchRoleDialog = false
                user?.uid?.let { viewModel.switchRole(it) }
            },
            onDismiss = { showSwitchRoleDialog = false }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            DonorHeader(
                donorFirstName = user?.displayName?.takeIf { it.isNotBlank() }?.substringBefore(' '),
                onSwitchRoleClick = { showSwitchRoleDialog = true }
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(16.dp))
                EligibilityCard(lastDonationMillis = user?.lastDonationDateMillis)
                Spacer(Modifier.height(14.dp))
                NutritionTipCard()
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 20.dp)
            ) {
                Text("Active requests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                FilterChip(
                    selected = state.compatibleOnly,
                    onClick = { viewModel.toggleCompatibleOnly() },
                    label = { Text(if (state.compatibleOnly) "Compatible with me" else "Showing all") }
                )
            }
        }

        if (state.visibleRequests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
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
private fun NutritionTipCard() {
    val tips = remember {
        listOf(
            "Pair iron-rich foods like red meat, spinach, or lentils with vitamin C (citrus, tomatoes, bell peppers) — it roughly triples how much iron your body absorbs." to "Iron + Vitamin C",
            "Beans, tofu, and fortified cereals are strong plant-based iron sources if you're vegetarian or vegan." to "Plant-based iron",
            "Give your body 24–48 hours and plenty of water after donating before your next hard workout." to "Post-donation recovery",
            "Cooking in a cast-iron pan can meaningfully increase the iron content of acidic foods like tomato sauce." to "Kitchen tip"
        )
    }
    val dayIndex = remember { (System.currentTimeMillis() / (1000 * 60 * 60 * 24) % tips.size).toInt() }
    val (tip, label) = tips[dayIndex]

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = SageGreenLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SageGreen.copy(alpha = 0.25f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Restaurant, contentDescription = null, tint = SageGreen)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = SageGreen)
                Spacer(Modifier.height(4.dp))
                Text(tip, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
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

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                CircularProgressIndicator(
                    progress = { progress },
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round,
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = if (eligible) "Ready" else "${daysRemaining}d",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    if (eligible) "You're eligible to donate!" else "Almost there",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (eligible) {
                        "Your body has fully replenished — you're good to give again."
                    } else {
                        "$daysRemaining of $windowDays days left until your next eligible donation."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}
