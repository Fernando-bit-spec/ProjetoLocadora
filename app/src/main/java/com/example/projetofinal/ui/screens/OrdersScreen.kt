package com.example.projetofinal.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.projetofinal.domain.model.Cliente
import com.example.projetofinal.domain.model.Pedido
import com.example.projetofinal.domain.model.Produto
import com.example.projetofinal.domain.model.StatusPedido
import com.example.projetofinal.ui.components.AppBottomBar
import com.example.projetofinal.ui.components.AppTopBar
import com.example.projetofinal.ui.components.DeleteConfirmationDialog
import com.example.projetofinal.ui.components.SearchBarComponent
import com.example.projetofinal.ui.navigation.Screen
import com.example.projetofinal.ui.viewmodel.OrderViewModel
import java.util.Locale

@Composable
fun OrdersScreen(
    viewModel: OrderViewModel,
    onNavigateToRoute: (String) -> Unit,
    onLogout: () -> Unit
) {
    val pedidos by viewModel.pedidos.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val produtos by viewModel.produtos.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val isDialogVisible by viewModel.isDialogVisible.collectAsState()
    val editingPedido by viewModel.editingPedido.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var deleteCandidateId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackBarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Pedidos",
                onSettingsClick = { onNavigateToRoute(Screen.Settings.route) },
                onLogoutClick = onLogout
            )
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = Screen.Orders.route,
                onNavigateToRoute = onNavigateToRoute
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openDialog() }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Pedido")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBarComponent(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                placeholder = "Buscar pedido por cliente ou produto..."
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { viewModel.onStatusFilterSelect(null) },
                    label = { Text("Todos") }
                )
                StatusPedido.values().forEach { st ->
                    FilterChip(
                        selected = statusFilter == st,
                        onClick = { viewModel.onStatusFilterSelect(st) },
                        label = { Text(st.name) }
                    )
                }
            }

            if (pedidos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum pedido encontrado.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pedidos, key = { it.id }) { ped ->
                        OrderCard(
                            pedido = ped,
                            onEdit = { viewModel.openDialog(ped) },
                            onDelete = { deleteCandidateId = ped.id }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (isDialogVisible) {
        OrderFormDialog(
            pedido = editingPedido,
            clientes = clientes,
            produtos = produtos,
            onDismiss = { viewModel.closeDialog() },
            onSave = { clienteId, produtoId, qtd, st ->
                viewModel.savePedido(clienteId, produtoId, qtd, st)
            }
        )
    }

    if (deleteCandidateId != null) {
        DeleteConfirmationDialog(
            title = "Excluir Pedido",
            message = "Tem certeza de que deseja excluir este pedido?",
            onConfirm = {
                viewModel.deletePedido(deleteCandidateId!!)
                deleteCandidateId = null
            },
            onDismiss = { deleteCandidateId = null }
        )
    }
}

@Composable
private fun OrderCard(
    pedido: Pedido,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (statusBg, statusFg) = when (pedido.status) {
        StatusPedido.CONCLUIDO -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        StatusPedido.PENDENTE -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        StatusPedido.CANCELADO -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pedido #${pedido.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = pedido.status.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusFg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cliente: ${pedido.clienteNome}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Produto: ${pedido.produtoNome} (x${pedido.quantidade})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.getDefault(), "Total: R$ %.2f - %s", pedido.valorTotal, pedido.data),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderFormDialog(
    pedido: Pedido?,
    clientes: List<Cliente>,
    produtos: List<Produto>,
    onDismiss: () -> Unit,
    onSave: (Int, Int, String, StatusPedido) -> Unit
) {
    var selectedClienteId by remember { mutableStateOf(pedido?.clienteId ?: clientes.firstOrNull()?.id ?: 0) }
    var selectedProdutoId by remember { mutableStateOf(pedido?.produtoId ?: produtos.firstOrNull()?.id ?: 0) }
    var quantidade by remember { mutableStateOf(pedido?.quantidade?.toString() ?: "1") }
    var selectedStatus by remember { mutableStateOf(pedido?.status ?: StatusPedido.PENDENTE) }

    var clienteExpanded by remember { mutableStateOf(false) }
    var produtoExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    val selectedClienteName = clientes.find { it.id == selectedClienteId }?.nome ?: "Selecione um Cliente"
    val selectedProdutoName = produtos.find { it.id == selectedProdutoId }?.nome ?: "Selecione um Produto"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (pedido == null) "Novo Pedido" else "Editar Pedido") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Cliente Dropdown
                ExposedDropdownMenuBox(
                    expanded = clienteExpanded,
                    onExpandedChange = { clienteExpanded = !clienteExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedClienteName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cliente") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clienteExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = clienteExpanded,
                        onDismissRequest = { clienteExpanded = false }
                    ) {
                        clientes.forEach { cli ->
                            DropdownMenuItem(
                                text = { Text(cli.nome) },
                                onClick = {
                                    selectedClienteId = cli.id
                                    clienteExpanded = false
                                }
                            )
                        }
                    }
                }

                // Produto Dropdown
                ExposedDropdownMenuBox(
                    expanded = produtoExpanded,
                    onExpandedChange = { produtoExpanded = !produtoExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedProdutoName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Produto") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = produtoExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = produtoExpanded,
                        onDismissRequest = { produtoExpanded = false }
                    ) {
                        produtos.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text("${prod.nome} (R$ ${prod.preco})") },
                                onClick = {
                                    selectedProdutoId = prod.id
                                    produtoExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedStatus.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status do Pedido") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        StatusPedido.values().forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.name) },
                                onClick = {
                                    selectedStatus = st
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(selectedClienteId, selectedProdutoId, quantidade, selectedStatus)
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
