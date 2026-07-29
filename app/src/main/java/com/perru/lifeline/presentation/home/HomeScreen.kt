package com.perru.lifeline.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.perru.lifeline.R
import com.perru.lifeline.domain.model.UserRole
import com.perru.lifeline.presentation.common.RequestCard
import com.perru.lifeline.ui.theme.Crimson
import com.perru.lifeline.ui.theme.CreamSurface
import com.perru.lifeline.ui.theme.Terracotta

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onContinueAsDonor: () -> Unit,
    onContinueAsHospital: () -> Unit,
    onSignIn: () -> Unit,
    onGoToDashboard: () -> Unit,
    onRequestClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val user = state.currentUser

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            HomeHeader(
                signedInName = user?.displayName?.takeIf { it.isNotBlank() },
                hasRole = user?.role == UserRole.DONOR || user?.role == UserRole.HOSPITAL,
                onContinueAsDonor = onContinueAsDonor,
                onContinueAsHospital = onContinueAsHospital,
                onSignIn = onSignIn,
                onGoToDashboard = onGoToDashboard
            )
        }

        item {
            Text(
                "Active requests nearby",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        if (state.requests.isEmpty()) {
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
            items(state.requests, key = { it.id }) { request ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 7.dp)) {
                    RequestCard(request = request, onClick = { onRequestClick(request.id) })
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun HomeHeader(
    signedInName: String?,
    hasRole: Boolean,
    onContinueAsDonor: () -> Unit,
    onContinueAsHospital: () -> Unit,
    onSignIn: () -> Unit,
    onGoToDashboard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Terracotta, Crimson)),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(CreamSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_lifeline),
                        contentDescription = null,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("LifeLine", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CreamSurface)
                    Text(
                        if (signedInName != null) "Welcome back, ${signedInName.substringBefore(' ')}"
                        else "Real-time blood requests near you",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CreamSurface.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            if (hasRole) {
                Button(
                    onClick = onGoToDashboard,
                    colors = ButtonDefaults.buttonColors(containerColor = CreamSurface, contentColor = Crimson),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Go to my dashboard", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onContinueAsDonor,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CreamSurface),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, CreamSurface),
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("I'm a Donor")
                    }
                    Button(
                        onClick = onContinueAsHospital,
                        colors = ButtonDefaults.buttonColors(containerColor = CreamSurface, contentColor = Crimson),
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Filled.LocalHospital, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("I'm a Hospital")
                    }
                }
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
                    Text("Already have an account? Sign in", color = CreamSurface)
                }
            }
        }
    }
}
