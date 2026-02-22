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
import android.net.Uri
import com.swiftflow.presentation.auth.AuthViewModel
import com.swiftflow.presentation.auth.LoginScreen
import com.swiftflow.presentation.common.MainScreen
import com.swiftflow.presentation.chat.ChatScreen
import com.swiftflow.presentation.delivery.DeliveryDetailScreen
import com.swiftflow.presentation.delivery.editor.PhotoEditorScreen
import com.swiftflow.presentation.delivery.wizard.CreateDeliveryWizardScreen
import com.swiftflow.presentation.product.ProductFormScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Main : Screen("main")
    data object CreateDelivery : Screen("create_delivery")
    data object DeliveryDetail : Screen("delivery_detail/{deliveryId}") {
        fun createRoute(deliveryId: Int) = "delivery_detail/$deliveryId"
    }
    data object PhotoEditor : Screen("photo_editor/{deliveryId}/{photoId}/{photoUrl}") {
        fun createRoute(deliveryId: Int, photoId: Int, photoUrl: String): String {
            val encodedUrl = Uri.encode(photoUrl)
            return "photo_editor/$deliveryId/$photoId/$encodedUrl"
        }
    }
    data object Chat : Screen("chat/{deliveryId}") {
        fun createRoute(deliveryId: Int) = "chat/$deliveryId"
    }
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
                onDeliveryClick = { deliveryId ->
                    navController.navigate(Screen.DeliveryDetail.createRoute(deliveryId))
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

        composable(
            route = Screen.DeliveryDetail.route,
            arguments = listOf(
                navArgument("deliveryId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val deliveryId = backStackEntry.arguments?.getInt("deliveryId") ?: return@composable
            DeliveryDetailScreen(
                deliveryId = deliveryId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPhotoEditor = { delId, photoId, photoUrl ->
                    navController.navigate(Screen.PhotoEditor.createRoute(delId, photoId, photoUrl))
                },
                onNavigateToChat = { delId ->
                    navController.navigate(Screen.Chat.createRoute(delId))
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("deliveryId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val deliveryId = backStackEntry.arguments?.getInt("deliveryId") ?: return@composable
            ChatScreen(
                deliveryId = deliveryId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.PhotoEditor.route,
            arguments = listOf(
                navArgument("deliveryId") { type = NavType.IntType },
                navArgument("photoId") { type = NavType.IntType },
                navArgument("photoUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deliveryId = backStackEntry.arguments?.getInt("deliveryId") ?: return@composable
            val photoId = backStackEntry.arguments?.getInt("photoId") ?: return@composable
            val photoUrl = backStackEntry.arguments?.getString("photoUrl")?.let { Uri.decode(it) } ?: return@composable

            PhotoEditorScreen(
                deliveryId = deliveryId,
                photoId = photoId,
                photoUrl = photoUrl,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreateDelivery.route) {
            CreateDeliveryWizardScreen(
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
