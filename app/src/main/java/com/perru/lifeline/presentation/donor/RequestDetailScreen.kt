package com.perru.lifeline.presentation.donor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.perru.lifeline.domain.model.RequestStatus
import com.perru.lifeline.presentation.common.BloodGroupChip
import com.perru.lifeline.presentation.common.UrgencyBadge

import kotlin.collections.find

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(requestId: String, viewModel: DonorViewModel = hiltViewModel(), onBack: () -> Unit) {
    val feedState by viewModel.uiState.collectAsState()
    val request = feedState.allRequests.find { it.id == requestId }
    var pledgeInFlight by remember { mutableStateOf(false) }
    var pledgeConfirmed by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
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
            if (request.contactPhone.isNotBlank()) DetailRow("Contact", request.contactPhone)

            if (request.notes.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text("Notes from the hospital", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(request.notes, style = MaterialTheme.typography.bodyMedium)
            }

            if (request.verificationImageUrl.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text("Verification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = request.verificationImageUrl,
                    contentDescription = "Hospital verification document",
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }

            errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.weight(1f))

            val donor = feedState.currentUser
            val alreadyFulfilled = request.status == RequestStatus.FULFILLED

            Button(
                onClick = {
                    if (donor == null) return@Button
                    pledgeInFlight = true
                    viewModel.pledge(request, donor) { result ->
                        pledgeInFlight = false
                        result.onSuccess { pledgeConfirmed = true }.onFailure { errorMessage = it.message }
                    }
                },
                enabled = !pledgeInFlight && !pledgeConfirmed && !alreadyFulfilled,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                if (pledgeInFlight) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(when {
                        pledgeConfirmed -> "Pledge confirmed 🎉"
                        alreadyFulfilled -> "Request fulfilled"
                        else -> "I'm Pledging to Donate"
                    })
                }
            }

            if (request.contactPhone.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { /* Wire to an Intent(Intent.ACTION_DIAL) in the calling Activity */ }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Call hospital")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}