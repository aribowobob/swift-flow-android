package com.swiftflow.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.swiftflow.presentation.auth.AuthViewModel
import com.swiftflow.presentation.auth.LoginScreen
import com.swiftflow.presentation.delivery.DeliveryListScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object DeliveryList : Screen("deliveries")
    data object CreateDelivery : Screen("create_delivery")
    data object ProductList : Screen("products")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.state.collectAsState()

    val startDestination = if (authState.isLoggedIn) {
        Screen.DeliveryList.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.DeliveryList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DeliveryList.route) {
            DeliveryListScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCreateDelivery = {
                    navController.navigate(Screen.CreateDelivery.route)
                }
            )
        }

        composable(Screen.CreateDelivery.route) {
            // CreateDeliveryScreen will be created later
            // Placeholder for now
        }

        composable(Screen.ProductList.route) {
            // ProductListScreen will be created later
            // Placeholder for now
        }
    }
}
