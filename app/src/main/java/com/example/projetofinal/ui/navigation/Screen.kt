package com.example.projetofinal.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Home : Screen("home", "Início", Icons.Default.Home)
    object Categories : Screen("categories", "Categorias", Icons.Default.Category)
    object Products : Screen("products", "Produtos", Icons.Default.ShoppingBag)
    object Customers : Screen("customers", "Clientes", Icons.Default.People)
    object Orders : Screen("orders", "Pedidos", Icons.Default.ShoppingCart)
    object Settings : Screen("settings", "Configurações", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(
            Home,
            Categories,
            Products,
            Customers,
            Orders
        )
    }
}
