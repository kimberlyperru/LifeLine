package com.perru.lifeline.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.perru.lifeline.domain.model.BloodGroup
import com.perru.lifeline.domain.model.UserRole

@Composable
fun RoleSelectionScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onOnboardingComplete: () -> Unit
) {
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    val onboardingState by viewModel.onboardingState.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf<BloodGroup?>(null) }
    var hospitalName by remember { mutableStateOf("") }

    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        if (selectedRole == null) {
            Text("How will you use LifeLine?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("You can always reach out to us if this needs to change later.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))

            RoleCard(
                title = "I'm a Donor",
                subtitle = "Browse nearby requests, track eligibility, and stay donation-ready.",
                icon = Icons.Filled.Favorite,
                onClick = { selectedRole = UserRole.DONOR }
            )
            Spacer(Modifier.height(16.dp))
            RoleCard(
                title = "I'm a Hospital / Clinic",
                subtitle = "Post urgent blood requests and track pledged donors.",
                icon = Icons.Filled.LocalHospital,
                onClick = { selectedRole = UserRole.HOSPITAL }
            )
        } else {
            Text(
                if (selectedRole == UserRole.DONOR) "Tell us about you" else "Tell us about your facility",
                style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = displayName, onValueChange = { displayName = it },
                label = { Text(if (selectedRole == UserRole.DONOR) "Full name" else "Contact person name") },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
            )

            if (selectedRole == UserRole.DONOR) {
                Spacer(Modifier.height(16.dp))
                Text("Blood group", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BloodGroup.entries) { group ->
                        FilterChip(selected = bloodGroup == group, onClick = { bloodGroup = group }, label = { Text(group.label) })
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = hospitalName, onValueChange = { hospitalName = it }, label = { Text("Hospital / clinic name") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
                )
            }

            onboardingState.errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            val canSubmit = displayName.isNotBlank() && city.isNotBlank() &&
                    (selectedRole == UserRole.HOSPITAL || bloodGroup != null) &&
                    (selectedRole == UserRole.DONOR || hospitalName.isNotBlank())

            Button(
                onClick = {
                    viewModel.completeOnboarding(
                        uid = uid, email = email, role = selectedRole!!,
                        form = OnboardingFormState(
                            displayName = displayName,
                            city = city,
                            bloodGroup = bloodGroup,
                            hospitalName = hospitalName
                        ),
                        onSuccess = onOnboardingComplete
                    )
                },
                enabled = canSubmit && !onboardingState.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                if (onboardingState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Continue")
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { selectedRole = null }) { Text("Back") }
        }
    }
}

@Composable
private fun RoleCard(
    title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}