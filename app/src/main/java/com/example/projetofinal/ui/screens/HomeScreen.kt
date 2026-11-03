package com.example.projetofinal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.projetofinal.ui.components.AppBottomBar
import com.example.projetofinal.ui.components.AppTopBar
import com.example.projetofinal.ui.navigation.Screen
import com.example.projetofinal.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    userName: String,
    viewModel: MainViewModel,
    onNavigateToRoute: (String) -> Unit,
    onLogout: () -> Unit
) {
    val summary by viewModel.summary.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Painel Principal",
                onSettingsClick = { onNavigateToRoute(Screen.Settings.route) },
                onLogoutClick = onLogout
            )
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = Screen.Home.route,
                onNavigateToRoute = onNavigateToRoute
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Olá, $userName!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bem-vindo ao Sistema de Gestão. Confira o resumo do seu negócio abaixo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Resumo Geral",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    SummaryCard(
                        title = "Categorias",
                        value = "${summary.totalCategorias}",
                        icon = Icons.Default.Category,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { onNavigateToRoute(Screen.Categories.route) }
                    )
                }
                item {
                    SummaryCard(
                        title = "Produtos",
                        value = "${summary.totalProdutos}",
                        icon = Icons.Default.ShoppingBag,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = { onNavigateToRoute(Screen.Products.route) }
                    )
                }
                item {
                    SummaryCard(
                        title = "Clientes",
                        value = "${summary.totalClientes}",
                        icon = Icons.Default.People,
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = { onNavigateToRoute(Screen.Customers.route) }
                    )
                }
                item {
                    SummaryCard(
                        title = "Pedidos",
                        value = "${summary.totalPedidos}",
                        icon = Icons.Default.ShoppingCart,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { onNavigateToRoute(Screen.Orders.route) }
                    )
                }
                item {
                    SummaryCard(
                        title = "Faturamento",
                        value = String.format("R$ %.2f", summary.faturamentoTotal),
                        icon = Icons.Default.MonetizationOn,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = { onNavigateToRoute(Screen.Orders.route) }
                    )
                }
                item {
                    SummaryCard(
                        title = "Pendentes",
                        value = "${summary.pedidosPendentes}",
                        icon = Icons.Default.PendingActions,
                        color = MaterialTheme.colorScheme.error,
                        onClick = { onNavigateToRoute(Screen.Orders.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )

            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
