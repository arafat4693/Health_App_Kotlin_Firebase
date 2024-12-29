package com.example.myhealth.views


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myhealth.viewModels.AuthenticationViewModel
import com.example.myhealth.viewModels.WaterTrackerViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object WaterTracker : Screen("water_tracker")
}

@Composable
fun AppNavigation(viewModel: AuthenticationViewModel, waterTrackerViewModel: WaterTrackerViewModel) {
    val navController = rememberNavController()

    LaunchedEffect(viewModel.isUserLoggedIn.value) {
        if (viewModel.isUserLoggedIn.value) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = if (viewModel.isUserLoggedIn.value) Screen.Home.route else Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } },
                viewModel = viewModel
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } },
                onBackToLogin = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(onNavigateToWaterTracker = { navController.navigate(Screen.WaterTracker.route) }) // Pass navigation function
        }
        composable(Screen.WaterTracker.route) {
            WaterTrackerScreen(viewModel = waterTrackerViewModel)
        }
    }
}
