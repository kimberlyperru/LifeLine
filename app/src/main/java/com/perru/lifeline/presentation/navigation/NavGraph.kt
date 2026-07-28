package com.perru.lifeline.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.perru.lifeline.presentation.hospital.CreateRequestScreen
import com.perru.lifeline.presentation.hospital.HospitalDashboardScreen

@Composable
fun LifeLineNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isSignedIn = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null

    // Redirect whenever the signed-in user's role resolves or their session ends,
    // so a role change or sign-out anywhere in the app snaps navigation back in sync.
    LaunchedEffect(isSignedIn, currentUser?.role) {
        if (!isSignedIn) {
            navController.navigateToRoot(Screen.Login.route)
        } else if (currentUser != null) {
            when (currentUser?.role) {
                UserRole.DONOR -> navController.navigateToRoot(Screen.DonorFeed.route)
                UserRole.HOSPITAL -> navController.navigateToRoot(Screen.HospitalDashboard.route)
                UserRole.UNSET, null -> navController.navigateToRoot(Screen.RoleSelection.route)
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
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
            RoleSelectionScreen(onOnboardingComplete = { /* navigation handled by LaunchedEffect above */ })
        }
        composable(Screen.DonorFeed.route) {
            DonorFeedScreen(onRequestClick = { requestId -> navController.navigate(Screen.RequestDetail.createRoute(requestId)) })
        }
        composable(Screen.RequestDetail.route) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId").orEmpty()
            RequestDetailScreen(requestId = requestId, onBack = { navController.popBackStack() })
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