package com.perru.lifeline.presentation.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.perru.lifeline.domain.model.BloodGroup
import com.perru.lifeline.domain.model.UserRole
import com.perru.lifeline.ui.theme.Crimson
import com.perru.lifeline.ui.theme.CrimsonLight
import com.perru.lifeline.ui.theme.SageGreen
import com.perru.lifeline.ui.theme.SageGreenDark
import com.perru.lifeline.ui.theme.SageGreenLight

@Composable
fun RoleSelectionScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    preselectedRole: UserRole? = null,
    onOnboardingComplete: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(preselectedRole) }
    val onboardingState by viewModel.onboardingState.collectAsState()
    val haptic = LocalHapticFeedback.current

    var displayName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf<BloodGroup?>(null) }
    var hospitalName by remember { mutableStateOf("") }

    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        // Progress Stepper
        LinearProgressIndicator(
            progress = { if (selectedRole == null) 0.5f else 1.0f },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = if (selectedRole == UserRole.HOSPITAL) SageGreen else Crimson,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = selectedRole,
                transitionSpec = {
                    if (targetState != null) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "RoleSelectionContent"
            ) { role ->
                if (role == null) {
                    Column {
                        Text("How will you use LifeLine?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "You can always reach out to us if this needs to change later.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(28.dp))

                        RoleCard(
                            title = "I'm a Donor",
                            subtitle = "Browse nearby requests, track eligibility, and stay donation-ready.",
                            icon = Icons.Filled.Favorite,
                            accent = Crimson,
                            accentSoft = CrimsonLight,
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedRole = UserRole.DONOR 
                            }
                        )
                        Spacer(Modifier.height(16.dp))
                        RoleCard(
                            title = "I'm a Hospital / Clinic",
                            subtitle = "Post urgent blood requests and track pledged donors.",
                            icon = Icons.Filled.LocalHospital,
                            accent = SageGreen,
                            accentSoft = SageGreenLight,
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedRole = UserRole.HOSPITAL 
                            }
                        )
                    }
                } else {
                    Column {
                        Text(
                            if (role == UserRole.DONOR) "Tell us about you" else "Tell us about your facility",
                            style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = displayName, onValueChange = { displayName = it },
                            label = { Text(if (role == UserRole.DONOR) "Full name" else "Contact person name") },
                            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
                        )

                        if (role == UserRole.DONOR) {
                            Spacer(Modifier.height(16.dp))
                            Text("Blood group", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(BloodGroup.entries) { group ->
                                    FilterChip(
                                        selected = bloodGroup == group,
                                        onClick = { 
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            bloodGroup = group 
                                        },
                                        label = { Text(group.label) }
                                    )
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
                            (role == UserRole.HOSPITAL || bloodGroup != null) &&
                            (role == UserRole.DONOR || hospitalName.isNotBlank())

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.completeOnboarding(
                                    uid = uid, email = email, role = role,
                                    form = OnboardingFormState(displayName = displayName, city = city, bloodGroup = bloodGroup, hospitalName = hospitalName),
                                    onSuccess = onOnboardingComplete
                                )
                            },
                            enabled = canSubmit && !onboardingState.isLoading,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (role == UserRole.HOSPITAL) SageGreenDark else Crimson
                            )
                        ) {
                            if (onboardingState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Text("Continue")
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedRole = null 
                        }) { Text("Back") }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleCard(
    title: String, subtitle: String, icon: ImageVector, accent: Color, accentSoft: Color, onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).background(accentSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
