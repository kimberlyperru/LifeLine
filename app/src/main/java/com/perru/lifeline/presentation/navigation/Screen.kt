package com.perru.lifeline.presentation.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")

    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object RoleSelection : Screen("role_selection")

    data object DonorFeed : Screen("donor_feed")
    data object RequestDetail : Screen("request_detail/{requestId}") {
        fun createRoute(requestId: String) = "request_detail/$requestId"
    }

    data object HospitalDashboard : Screen("hospital_dashboard")
    data object CreateRequest : Screen("create_request")
}
