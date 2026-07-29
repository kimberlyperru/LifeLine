package com.perru.lifeline.presentation.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.perru.lifeline.R
import com.perru.lifeline.ui.theme.CreamSurface
import com.perru.lifeline.ui.theme.SageGreen
import com.perru.lifeline.ui.theme.SageGreenDark
import com.perru.lifeline.ui.theme.Terracotta

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.signUpState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    val passwordsMatch = password == confirmPassword
    val canSubmit = email.isNotBlank() && password.length >= 6 && passwordsMatch && !state.isLoading

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Animated Header transition
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(SageGreen, SageGreenDark)),
                    shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)
                )
                .padding(top = 56.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(84.dp).clip(CircleShape).background(CreamSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_lifeline),
                        contentDescription = "LifeLine logo",
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text("Join LifeLine", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CreamSurface)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Every donor and every hospital starts here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CreamSurface.copy(alpha = 0.9f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 32.dp)
        ) {
            OutlinedTextField(
                value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it }, label = { Text("Password (min. 6 characters)") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
            )
            
            // Password Strength Indicator
            if (password.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                val strength = when {
                    password.length < 6 -> 0.2f
                    password.length < 10 -> 0.6f
                    else -> 1.0f
                }
                val color = when {
                    strength < 0.5f -> Color(0xFFB3453E) // UrgencyCritical
                    strength < 0.8f -> Color(0xFFD9B23D) // UrgencyModerate
                    else -> SageGreen
                }
                LinearProgressIndicator(
                    progress = { strength },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = color,
                    trackColor = color.copy(alpha = 0.1f)
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm password") }, singleLine = true,
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
            )

            state.errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.signUp(email, password, onSignUpSuccess) 
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenDark),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = MaterialTheme.shapes.large
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Create Account")
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Already have an account? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Sign in",
                    color = SageGreenDark,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToLogin() 
                    }
                )
            }
        }
    }
}
