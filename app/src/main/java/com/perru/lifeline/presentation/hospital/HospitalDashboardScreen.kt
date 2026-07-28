package com.perru.lifeline.presentation.hospital

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.perru.lifeline.presentation.common.RequestCard


@Composable
fun HospitalDashboardScreen(viewModel: HospitalViewModel = hiltViewModel(), onCreateRequest: () -> Unit) {
    val hospital by viewModel.currentUser.collectAsState()
    val requests by viewModel.myRequests.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateRequest,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New request") }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                Text(hospital?.hospitalName?.ifBlank { "Your facility" } ?: "Your facility", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Track your posted requests and pledged donors.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (requests.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No requests posted yet. Tap \"New request\" to reach nearby donors.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(requests, key = { it.id }) { request ->
                    RequestCard(request = request, onClick = { /* Detail/edit view — extend as needed */ })
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}