package com.perru.lifeline.presentation.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.perru.lifeline.domain.model.BloodRequest
import com.perru.lifeline.domain.model.UrgencyLevel
import com.perru.lifeline.ui.theme.UrgencyCritical
import com.perru.lifeline.ui.theme.UrgencyHigh
import com.perru.lifeline.ui.theme.UrgencyModerate
import com.perru.lifeline.util.TimeUtils

fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    background(brush)
}

@Composable
fun ShimmerRequestCard(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp, 28.dp).clip(RoundedCornerShape(12.dp)).shimmer())
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.size(60.dp, 12.dp).shimmer())
                }
                Box(modifier = Modifier.size(70.dp, 24.dp).clip(RoundedCornerShape(50)).shimmer())
            }
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.size(140.dp, 18.dp).shimmer())
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.size(100.dp, 14.dp).shimmer())
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.size(120.dp, 14.dp).shimmer())
        }
    }
}

fun UrgencyLevel.color(): Color = when (this) {
    UrgencyLevel.CRITICAL -> UrgencyCritical
    UrgencyLevel.HIGH -> UrgencyHigh
    UrgencyLevel.MODERATE -> UrgencyModerate
}

@Composable
fun UrgencyBadge(urgency: UrgencyLevel, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(urgency.color().copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = urgency.label,
            color = urgency.color(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun BloodGroupChip(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RequestCard(
    request: BloodRequest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BloodGroupChip(label = request.bloodGroup.label)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = TimeUtils.toRelativeTime(request.createdAtMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                UrgencyBadge(urgency = request.urgency)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = request.hospitalName.ifBlank { "Hospital request" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${request.hospitalCity} • ${request.component.name.replace('_', ' ').lowercase()
                    .replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${request.unitsPledged}/${request.unitsNeeded} units pledged",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
