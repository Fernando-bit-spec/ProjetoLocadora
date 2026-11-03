package com.example.projetofinal.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projetofinal.data.datastore.UserPreferencesRepository
import com.example.projetofinal.data.repository.AppRepository
import com.example.projetofinal.ui.screens.CategoriesScreen
import com.example.projetofinal.ui.screens.CustomersScreen
import com.example.projetofinal.ui.screens.HomeScreen
import com.example.projetofinal.ui.screens.LoginScreen
import com.example.projetofinal.ui.screens.OrdersScreen
import com.example.projetofinal.ui.screens.ProductsScreen
import com.example.projetofinal.ui.screens.SettingsScreen
import com.example.projetofinal.ui.viewmodel.AuthViewModel
import com.example.projetofinal.ui.viewmodel.CategoryViewModel
import com.example.projetofinal.ui.viewmodel.CustomerViewModel
import com.example.projetofinal.ui.viewmodel.MainViewModel
import com.example.projetofinal.ui.viewmodel.OrderViewModel
import com.example.projetofinal.ui.viewmodel.ProductViewModel
import com.example.projetofinal.ui.viewmodel.SettingsViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    context: Context = LocalContext.current
) {
    val prefsRepository = remember { UserPreferencesRepository(context) }
    val appRepository = remember { AppRepository() }

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(prefsRepository) as T
            }
        }
    )

    val prefs by settingsViewModel.userPreferences.collectAsState()

    val authViewModel: AuthViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(appRepository, prefsRepository) as T
            }
        }
    )

    val mainViewModel: MainViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(appRepository) as T
            }
        }
    )

    val categoryViewModel: CategoryViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return CategoryViewModel(appRepository) as T
            }
        }
    )

    val productViewModel: ProductViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProductViewModel(appRepository) as T
            }
        }
    )

    val customerViewModel: CustomerViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return CustomerViewModel(appRepository) as T
            }
        }
    )

    val orderViewModel: OrderViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return OrderViewModel(appRepository) as T
            }
        }
    )

    val startDestination = if (prefs.isLoggedIn) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                userName = prefs.userName,
                viewModel = mainViewModel,
                onNavigateToRoute = { route -> navController.navigate(route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(
                viewModel = categoryViewModel,
                onNavigateToRoute = { route -> navController.navigate(route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Products.route) {
            ProductsScreen(
                viewModel = productViewModel,
                onNavigateToRoute = { route -> navController.navigate(route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Customers.route) {
            CustomersScreen(
                viewModel = customerViewModel,
                onNavigateToRoute = { route -> navController.navigate(route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Orders.route) {
            OrdersScreen(
                viewModel = orderViewModel,
                onNavigateToRoute = { route -> navController.navigate(route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToRoute = { route -> navController.navigate(route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
