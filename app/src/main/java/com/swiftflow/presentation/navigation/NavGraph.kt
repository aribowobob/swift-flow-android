package com.swiftflow.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.swiftflow.presentation.auth.AuthViewModel
import com.swiftflow.presentation.auth.LoginScreen
import com.swiftflow.presentation.common.MainScreen
import com.swiftflow.presentation.delivery.CreateDeliveryScreen
import com.swiftflow.presentation.product.ProductFormScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Main : Screen("main")
    data object CreateDelivery : Screen("create_delivery")
    data object CreateProduct : Screen("create_product")
    data object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: Int) = "edit_product/$productId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.state.collectAsState()
    var productSuccessMessage by remember { mutableStateOf<String?>(null) }

    val startDestination = if (authState.isLoggedIn) {
        Screen.Main.route
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
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            // Clear success message after showing
            LaunchedEffect(productSuccessMessage) {
                if (productSuccessMessage != null) {
                    // Message will be shown in ProductListScreen, wait for snackbar to display
                    kotlinx.coroutines.delay(2000)
                    productSuccessMessage = null
                }
            }

            MainScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCreateDelivery = {
                    navController.navigate(Screen.CreateDelivery.route)
                },
                onCreateProduct = {
                    navController.navigate(Screen.CreateProduct.route)
                },
                onEditProduct = { productId ->
                    navController.navigate(Screen.EditProduct.createRoute(productId))
                },
                productSuccessMessage = productSuccessMessage,
                authViewModel = authViewModel
            )
        }

        composable(Screen.CreateDelivery.route) {
            CreateDeliveryScreen(
                onDeliveryCreated = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreateProduct.route) {
            ProductFormScreen(
                productId = null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProductSaved = { message ->
                    productSuccessMessage = message
                }
            )
        }

        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId")
            ProductFormScreen(
                productId = productId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProductSaved = { message ->
                    productSuccessMessage = message
                }
            )
        }
    }
}
