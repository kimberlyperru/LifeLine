package com.perru.lifeline.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.perru.lifeline.domain.model.UserRole
import com.perru.lifeline.presentation.auth.AuthViewModel
import com.perru.lifeline.presentation.auth.LoginScreen
import com.perru.lifeline.presentation.auth.RoleSelectionScreen
import com.perru.lifeline.presentation.auth.SignUpScreen
import com.perru.lifeline.presentation.donor.DonorFeedScreen
import com.perru.lifeline.presentation.donor.RequestDetailScreen
import com.perru.lifeline.presentation.home.HomeScreen
import com.perru.lifeline.presentation.hospital.CreateRequestScreen
import com.perru.lifeline.presentation.hospital.HospitalDashboardScreen
import com.perru.lifeline.presentation.onboarding.OnboardingScreen
import com.perru.lifeline.presentation.splash.SplashScreen
import com.perru.lifeline.util.OnboardingPrefs

@Composable
fun LifeLineNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isSignedIn = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
    val context = LocalContext.current

    // Splash and onboarding are pure state-setters now — they don't navigate
    // themselves. Every "which root screen should be showing" decision funnels
    // through the single LaunchedEffect below, so there's exactly one source of
    // truth instead of two navigation triggers racing each other (which was
    // previously yanking people off the login screen before they could see it).
    var splashFinished by remember { mutableStateOf(false) }
    var onboardingSeen by remember { mutableStateOf(OnboardingPrefs.hasSeenOnboarding(context)) }

    // Set from Home's "I'm a Donor" / "I'm a Hospital" buttons so that, once the
    // person signs in, role selection opens pre-picked instead of asking again.
    var pendingRoleIntent by remember { mutableStateOf<UserRole?>(null) }

    LaunchedEffect(splashFinished, onboardingSeen, isSignedIn, currentUser?.role) {
        if (!splashFinished) return@LaunchedEffect
        val target = when {
            !onboardingSeen -> Screen.Onboarding.route
            !isSignedIn -> Screen.Home.route
            currentUser == null -> return@LaunchedEffect // profile still loading — hold current screen
            currentUser?.role == UserRole.DONOR -> Screen.DonorFeed.route
            currentUser?.role == UserRole.HOSPITAL -> Screen.HospitalDashboard.route
            else -> Screen.RoleSelection.route
        }
        navController.navigateToRoot(target)
    }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onFinished = { splashFinished = true })
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    OnboardingPrefs.markOnboardingSeen(context)
                    onboardingSeen = true
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onContinueAsDonor = {
                    pendingRoleIntent = UserRole.DONOR
                    navController.navigate(Screen.Login.route)
                },
                onContinueAsHospital = {
                    pendingRoleIntent = UserRole.HOSPITAL
                    navController.navigate(Screen.Login.route)
                },
                onSignIn = { navController.navigate(Screen.Login.route) },
                onGoToDashboard = { /* handled by the redirect effect once signed in with a role */ },
                onRequestClick = { requestId -> navController.navigate(Screen.RequestDetail.createRoute(requestId)) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { /* navigation handled by LaunchedEffect above */ },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = { /* navigation handled by LaunchedEffect above */ },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                preselectedRole = pendingRoleIntent,
                onOnboardingComplete = { pendingRoleIntent = null /* navigation handled by LaunchedEffect above */ }
            )
        }
        composable(Screen.DonorFeed.route) {
            DonorFeedScreen(onRequestClick = { requestId -> navController.navigate(Screen.RequestDetail.createRoute(requestId)) })
        }
        composable(Screen.RequestDetail.route) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId").orEmpty()
            RequestDetailScreen(
                requestId = requestId,
                onBack = { navController.popBackStack() },
                onRequireSignIn = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.HospitalDashboard.route) {
            HospitalDashboardScreen(onCreateRequest = { navController.navigate(Screen.CreateRequest.route) })
        }
        composable(Screen.CreateRequest.route) {
            CreateRequestScreen(onBack = { navController.popBackStack() }, onSubmitted = { navController.popBackStack() })
        }
    }
}

/** Clears back stack up to the graph root and navigates fresh — used for auth-driven redirects. */
private fun NavHostController.navigateToRoot(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
