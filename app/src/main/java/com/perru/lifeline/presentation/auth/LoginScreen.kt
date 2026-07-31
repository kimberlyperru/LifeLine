package com.perru.lifeline.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.perru.lifeline.R
import com.perru.lifeline.ui.theme.Crimson
import com.perru.lifeline.ui.theme.CreamSurface
import com.perru.lifeline.ui.theme.Terracotta
import com.perru.lifeline.util.GoogleSignInHelper
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuthHeader(
            title = stringResource(R.string.welcome_back),
            subtitle = stringResource(R.string.login_subtitle)
        )

        LoginContent(
            viewModel = viewModel,
            onLoginSuccess = onLoginSuccess,
            onNavigateToSignUp = onNavigateToSignUp
        )
    }
}

@Composable
fun AuthHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(Terracotta, Crimson)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(gradientColors),
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
                    contentDescription = stringResource(R.string.cd_logo),
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CreamSurface)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = CreamSurface.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun LoginContent(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.loginState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Error Shake Animation
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            repeat(4) {
                shakeOffset.animateTo(
                    targetValue = if (it % 2 == 0) 15f else -15f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
                )
            }
            shakeOffset.animateTo(0f)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 32.dp)
            .graphicsLayer { translationX = shakeOffset.value },
        verticalArrangement = Arrangement.Top
    ) {
        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.email)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text(stringResource(R.string.password)) }, singleLine = true,
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
                viewModel.signIn(email, password, onLoginSuccess) 
            },
            enabled = email.isNotBlank() && password.isNotBlank() && !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = MaterialTheme.shapes.large
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text(stringResource(R.string.sign_in))
            }
        }

        Spacer(Modifier.height(16.dp))
        
        // Social Login - Google
        OutlinedButton(
            onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                scope.launch {
                    GoogleSignInHelper.signInWithGoogle(context).onSuccess { idToken ->
                        viewModel.signInWithGoogleIdToken(idToken, onLoginSuccess)
                    }.onFailure {
                        // Error is handled by AuthViewModel
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            enabled = !state.isLoading,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.logo_lifeline), // Placeholder for Google Logo
                    contentDescription = stringResource(R.string.cd_google_icon),
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.continue_with_google), color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.new_to_lifeline), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                stringResource(R.string.create_account),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNavigateToSignUp() 
                }
            )
        }
    }
}
