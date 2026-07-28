package com.perru.lifeline.presentation.donor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.perru.lifeline.presentation.common.RequestCard
import com.perru.lifeline.util.BloodCompatibility
import java.util.concurrent.TimeUnit

@Composable
fun DonorFeedScreen(viewModel: DonorViewModel = hiltViewModel(), onRequestClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val user = state.currentUser

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                "Hi${if (user?.displayName?.isNotBlank() == true) ", ${user.displayName.substringBefore(' ')}" else ""} 👋",
                style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text("Thanks for being ready to save a life.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item { EligibilityCard(lastDonationMillis = user?.lastDonationDateMillis) }

        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
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
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No active requests right now — check back soon.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(state.visibleRequests, key = { it.id }) { request ->
                RequestCard(request = request, onClick = { onRequestClick(request.id) })
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun EligibilityCard(lastDonationMillis: Long?) {
    val windowDays = BloodCompatibility.ELIGIBILITY_WINDOW_DAYS
    val daysSince = lastDonationMillis?.let { TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it).toInt() }
    val daysRemaining = (daysSince?.let { windowDays - it } ?: 0).coerceIn(0, windowDays)
    val progress = if (lastDonationMillis == null) 1f else (daysSince!!.toFloat() / windowDays).coerceIn(0f, 1f)
    val eligible = lastDonationMillis == null || daysRemaining <= 0

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                CircularProgressIndicator(
                    progress = { progress }, strokeWidth = 6.dp, strokeCap = StrokeCap.Round,
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = if (eligible) "Ready" else "${daysRemaining}d",
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    if (eligible) "You're eligible to donate!" else "Almost there",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (eligible) "Your body has fully replenished — you're good to give again."
                    else "$daysRemaining of $windowDays days left until your next eligible donation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}