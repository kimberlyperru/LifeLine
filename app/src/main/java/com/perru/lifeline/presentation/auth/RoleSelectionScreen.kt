package com.perru.lifeline.presentation.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.perru.lifeline.R
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
    val scrollState = rememberScrollState()

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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .padding(bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_role_choice),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        
                        Text(
                            stringResource(R.string.how_use_lifeline),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.role_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(32.dp))

                        RoleCard(
                            title = "I want to save lives",
                            subtitle = stringResource(R.string.role_donor_subtitle),
                            icon = Icons.Filled.Favorite,
                            accent = Crimson,
                            accentSoft = CrimsonLight,
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedRole = UserRole.DONOR 
                            }
                        )
                        Spacer(Modifier.height(20.dp))
                        RoleCard(
                            title = "I need blood for patients",
                            subtitle = stringResource(R.string.role_hospital_subtitle),
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
                            if (role == UserRole.DONOR) stringResource(R.string.tell_us_about_you) else stringResource(R.string.tell_us_about_facility),
                            style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = displayName, onValueChange = { displayName = it },
                            label = { Text(if (role == UserRole.DONOR) stringResource(R.string.full_name) else stringResource(R.string.contact_person_name)) },
                            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = city, onValueChange = { city = it }, label = { Text(stringResource(R.string.city)) }, singleLine = true,
                            modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
                        )

                        if (role == UserRole.DONOR) {
                            Spacer(Modifier.height(16.dp))
                            Text(stringResource(R.string.blood_group), style = MaterialTheme.typography.labelLarge)
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
                                value = hospitalName, onValueChange = { hospitalName = it }, label = { Text(stringResource(R.string.hospital_clinic_name)) },
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
                                Text(stringResource(R.string.continue_btn))
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedRole = null 
                        }) { Text(stringResource(R.string.back_btn)) }
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(64.dp).background(accentSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
