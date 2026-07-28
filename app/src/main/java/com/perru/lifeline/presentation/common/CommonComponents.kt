package com.perru.lifeline.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.perru.lifeline.domain.model.BloodRequest
import com.perru.lifeline.domain.model.UrgencyLevel
import com.perru.lifeline.ui.theme.UrgencyCritical
import com.perru.lifeline.ui.theme.UrgencyHigh
import com.perru.lifeline.ui.theme.UrgencyLow
import com.perru.lifeline.ui.theme.UrgencyModerate

fun UrgencyLevel.color(): Color = when (this) {
    UrgencyLevel.CRITICAL -> UrgencyCritical
    UrgencyLevel.HIGH -> UrgencyHigh
    UrgencyLevel.MODERATE -> UrgencyModerate
}

@Composable
fun UrgencyBadge(
    urgency: UrgencyLevel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = urgency.color().copy(alpha = 0.15f),
                shape = CircleShape
            )
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
fun BloodGroupChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestCard(
    request: BloodRequest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedComponent = request.component.name
        .replace('_', ' ')
        .lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

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
                BloodGroupChip(label = request.bloodGroup.label)
                UrgencyBadge(urgency = request.urgency)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = request.hospitalName.ifBlank { "Hospital request" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${request.hospitalCity} • $formattedComponent",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${request.unitsPledged}/${request.unitsNeeded} units pledged",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}