package com.perru.lifeline.presentation.hospital

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.perru.lifeline.R
import com.perru.lifeline.presentation.common.RequestCard
import com.perru.lifeline.presentation.donor.SwitchRoleDialog
import com.perru.lifeline.ui.theme.Crimson
import com.perru.lifeline.ui.theme.CreamSurface
import com.perru.lifeline.ui.theme.SageGreen
import com.perru.lifeline.ui.theme.SageGreenDark

@Composable
fun HospitalDashboardScreen(
    viewModel: HospitalViewModel = hiltViewModel(),
    onCreateRequest: () -> Unit
) {
    val hospital by viewModel.currentUser.collectAsState()
    val requests by viewModel.myRequests.collectAsState()
    var showSwitchRoleDialog by remember { mutableStateOf(false) }

    if (showSwitchRoleDialog) {
        SwitchRoleDialog(
            targetRoleLabel = "Donor",
            onConfirm = {
                showSwitchRoleDialog = false
                hospital?.uid?.let { viewModel.switchRole(it) }
            },
            onDismiss = { showSwitchRoleDialog = false }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateRequest,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New request") }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                HospitalHeader(
                    hospitalName = hospital?.hospitalName?.ifBlank { null },
                    onSwitchRoleClick = { showSwitchRoleDialog = true }
                )
            }

            if (requests.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No requests posted yet. Tap \"New request\" to reach nearby donors.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(requests, key = { it.id }) { request ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 7.dp)) {
                        RequestCard(request = request, onClick = { /* Detail/edit view — extend as needed */ })
                    }
                }
            }

            item { Spacer(Modifier.height(88.dp)) } // room for the FAB
        }
    }
}

@Composable
private fun HospitalHeader(hospitalName: String?, onSwitchRoleClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(SageGreen, SageGreenDark)),
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
                    hospitalName ?: "Your facility",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CreamSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Track your posted requests and pledged donors.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CreamSurface.copy(alpha = 0.9f)
                )
            }
        }
    }
}
