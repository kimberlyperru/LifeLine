package com.perru.lifeline.presentation.donor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.perru.lifeline.R
import com.perru.lifeline.domain.model.BloodGroup
import com.perru.lifeline.domain.model.RequestStatus
import com.perru.lifeline.presentation.common.BloodGroupChip
import com.perru.lifeline.presentation.common.UrgencyBadge
import com.perru.lifeline.ui.theme.SageGreen
import com.perru.lifeline.ui.theme.SageGreenLight
import com.perru.lifeline.util.BloodCompatibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    requestId: String,
    viewModel: DonorViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onRequireSignIn: () -> Unit = {}
) {
    val feedState by viewModel.uiState.collectAsState()
    val requestSnapshot by viewModel.getRequestById(requestId).collectAsState(initial = null)
    val request = requestSnapshot
    val context = LocalContext.current
    var pledgeInFlight by remember { mutableStateOf(false) }
    var pledgeConfirmed by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Guests browsing without an account can still self-check compatibility by
    // picking their blood type here; signed-in donors get it from their profile.
    var guestBloodGroup by remember { mutableStateOf<BloodGroup?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request details") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        if (request == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        val donor = feedState.currentUser
        val donorBloodGroup = donor?.bloodGroup ?: guestBloodGroup
        val isCompatible = donorBloodGroup?.let { BloodCompatibility.isCompatible(it, request.bloodGroup) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BloodGroupChip(label = request.bloodGroup.label)
                Spacer(Modifier.width(12.dp))
                UrgencyBadge(urgency = request.urgency)
            }
            Spacer(Modifier.height(16.dp))
            Text(request.hospitalName.ifBlank { "Hospital request" }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(request.hospitalCity, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(20.dp))
            DetailRow("Component", request.component.name.replace('_', ' '))
            DetailRow("Units needed", "${request.unitsNeeded}")
            DetailRow("Units pledged", "${request.unitsPledged}")

            if (request.notes.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text("Notes from the hospital", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(request.notes, style = MaterialTheme.typography.bodyMedium)
            }

            if (request.verificationImageUrl.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text("Verification document", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    AsyncImage(
                        model = request.verificationImageUrl,
                        contentDescription = "Hospital verification document",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.ic_launcher_background), // Fallback placeholder
                        error = painterResource(id = R.drawable.ic_launcher_background) // Error placeholder
                    )
                }
            }

            // --- Compatibility check ---
            Spacer(Modifier.height(20.dp))
            if (donor?.bloodGroup == null) {
                Text("What's your blood type?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BloodGroup.entries) { group ->
                        FilterChip(
                            selected = guestBloodGroup == group,
                            onClick = { guestBloodGroup = group },
                            label = { Text(group.label) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (donorBloodGroup != null) {
                CompatibilityBanner(isCompatible = isCompatible == true)
                Spacer(Modifier.height(12.dp))
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.weight(1f))

            val alreadyFulfilled = request.status == RequestStatus.FULFILLED

            // Call/text are offered to anyone who looks like a match — browsing and
            // reaching out doesn't require an account; formally pledging does.
            if (isCompatible == true && request.contactPhone.isNotBlank() && !alreadyFulfilled) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${request.contactPhone}")))
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Call")
                    }
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${request.contactPhone}")).apply {
                                putExtra("sms_body", "Hi, I saw your LifeLine request for ${request.bloodGroup.label} blood and I'm a match. I'd like to help.")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Filled.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Text")
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Button(
                onClick = {
                    if (donor == null) {
                        onRequireSignIn()
                        return@Button
                    }
                    pledgeInFlight = true
                    viewModel.pledge(request, donor) { result ->
                        pledgeInFlight = false
                        result.onSuccess { pledgeConfirmed = true }.onFailure { errorMessage = it.message }
                    }
                },
                enabled = !pledgeInFlight && !pledgeConfirmed && !alreadyFulfilled && (donor == null || isCompatible == true),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                if (pledgeInFlight) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(when {
                        pledgeConfirmed -> "Pledge confirmed \uD83C\uDF89"
                        alreadyFulfilled -> "Request fulfilled"
                        donor == null -> "Sign in to pledge"
                        else -> "I'm Pledging to Donate"
                    })
                }
            }
        }
    }
}

@Composable
private fun CompatibilityBanner(isCompatible: Boolean) {
    val (bg, fg, message) = if (isCompatible) {
        Triple(SageGreenLight, SageGreen, "You're a match for this request \u2713")
    } else {
        Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, "Your blood type isn't a match for this request")
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(message, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
