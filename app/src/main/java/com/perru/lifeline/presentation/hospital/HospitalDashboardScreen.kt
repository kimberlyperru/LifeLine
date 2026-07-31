package com.perru.lifeline.presentation.hospital

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    val totalPledges by viewModel.totalPledges.collectAsState()
    val livesSaved by viewModel.livesSaved.collectAsState()
    val crisisMode by viewModel.crisisMode.collectAsState()
    var showSwitchRoleDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    if (showSwitchRoleDialog) {
        SwitchRoleDialog(
            targetRoleLabel = "Donor",
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showSwitchRoleDialog = false
                hospital?.uid?.let { viewModel.switchRole(it) }
            },
            onDismiss = { showSwitchRoleDialog = false }
        )
    }

    // Crisis mode subtle pulsing background
    val crisisColor = if (crisisMode) Crimson.copy(alpha = 0.05f) else Color.Transparent

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCreateRequest()
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_request_btn)) },
                containerColor = if (crisisMode) Crimson else MaterialTheme.colorScheme.primaryContainer
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(crisisColor).padding(padding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
            ) {
                item {
                    HospitalHeader(
                        hospitalName = hospital?.hospitalName?.ifBlank { null },
                        onSwitchRoleClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showSwitchRoleDialog = true 
                        },
                        onSignOutClick = { viewModel.signOut() }
                    )
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                        Text("Command Center", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(14.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                title = "Lives Saved",
                                value = livesSaved.toString(),
                                icon = Icons.Filled.VolunteerActivism,
                                modifier = Modifier.weight(1f),
                                color = SageGreen
                            )
                            StatCard(
                                title = "Pledges",
                                value = totalPledges.toString(),
                                icon = Icons.Filled.People,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Crisis Mode Toggle
                        Card(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleCrisisMode()
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (crisisMode) Crimson.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.NotificationsActive,
                                    contentDescription = stringResource(R.string.cd_crisis_toggle),
                                    tint = if (crisisMode) Crimson else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.emergency_mode), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(stringResource(R.string.emergency_mode_subtitle), style = MaterialTheme.typography.bodySmall)
                                }
                                Switch(
                                    checked = crisisMode,
                                    onCheckedChange = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.toggleCrisisMode()
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Crimson)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Your active requests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                if (requests.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(SageGreen.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = SageGreen
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "All quiet in the center",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No active blood requests right now. Tap \"New request\" to reach nearby donors.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 40.dp),
                                style = MaterialTheme.typography.bodyMedium
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
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HospitalHeader(hospitalName: String?, onSwitchRoleClick: () -> Unit, onSignOutClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(SageGreen, SageGreenDark)),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(modifier = Modifier.align(Alignment.TopEnd)) {
            IconButton(onClick = onSignOutClick) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.cd_logout), tint = CreamSurface)
            }
            IconButton(onClick = onSwitchRoleClick) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = stringResource(R.string.cd_switch_role), tint = CreamSurface)
            }
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
