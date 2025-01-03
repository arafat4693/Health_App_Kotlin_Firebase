package com.example.myhealth.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myhealth.viewModels.AuthenticationViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Goals : Screen("goals")
}

@Composable
fun AppNavigation(viewModel: AuthenticationViewModel) {
    val navController = rememberNavController()

    LaunchedEffect (viewModel.isUserLoggedIn.value) {
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
            HomeScreen(authViewModel = viewModel, toGoalsSettings = { navController.navigate(Screen.Goals.route) }, onLogOutSuccess = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } })
        }

        composable(Screen.Goals.route) {
            GoalsSettingsScreen(onNavigateBack = { navController.navigate(Screen.Home.route) })
        }
    }
}
