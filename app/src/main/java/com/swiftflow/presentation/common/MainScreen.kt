package com.swiftflow.presentation.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.swiftflow.domain.model.UserRole
import com.swiftflow.presentation.auth.AuthViewModel
import com.swiftflow.presentation.delivery.DashboardScreen
import com.swiftflow.presentation.delivery.DeliveryListScreen
import com.swiftflow.presentation.product.ProductListScreen
import com.swiftflow.presentation.settings.SettingsScreen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : BottomNavItem("dashboard", "Dashboard", Icons.Default.Home)
    data object Deliveries : BottomNavItem("deliveries", "Deliveries", Icons.Default.LocalShipping)
    data object Products : BottomNavItem("products", "Products", Icons.Default.ShoppingCart)
    data object Settings : BottomNavItem("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onCreateDelivery: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.state.collectAsState()
    var selectedItem by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Dashboard) }

    val userRole = authState.loginResponse?.user?.role

    // Define navigation items based on role
    val navItems = remember(userRole) {
        when (userRole) {
            UserRole.SUPERVISOR -> listOf(
                BottomNavItem.Dashboard,
                BottomNavItem.Deliveries,
                BottomNavItem.Products,
                BottomNavItem.Settings
            )
            UserRole.SALES -> listOf(
                BottomNavItem.Dashboard,
                BottomNavItem.Deliveries
            )
            else -> listOf(
                BottomNavItem.Dashboard,
                BottomNavItem.Deliveries
            )
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selectedItem == item,
                        onClick = { selectedItem = item }
                    )
                }
            }
        },
        floatingActionButton = {
            // Show FAB only for SALES users on Deliveries tab
            if (userRole == UserRole.SALES && selectedItem == BottomNavItem.Deliveries) {
                FloatingActionButton(onClick = onCreateDelivery) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Delivery"
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedItem) {
            BottomNavItem.Dashboard -> {
                DashboardScreen(
                    onLogout = onLogout,
                    authViewModel = authViewModel
                )
            }
            BottomNavItem.Deliveries -> {
                DeliveryListScreen(
                    onLogout = onLogout,
                    onCreateDelivery = onCreateDelivery
                )
            }
            BottomNavItem.Products -> {
                ProductListScreen(
                    onLogout = onLogout
                )
            }
            BottomNavItem.Settings -> {
                SettingsScreen(
                    onLogout = onLogout,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
