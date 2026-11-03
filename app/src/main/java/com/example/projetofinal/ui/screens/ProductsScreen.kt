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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.projetofinal.domain.model.Categoria
import com.example.projetofinal.domain.model.Produto
import com.example.projetofinal.ui.components.AppBottomBar
import com.example.projetofinal.ui.components.AppTopBar
import com.example.projetofinal.ui.components.DeleteConfirmationDialog
import com.example.projetofinal.ui.components.SearchBarComponent
import com.example.projetofinal.ui.navigation.Screen
import com.example.projetofinal.ui.viewmodel.ProductViewModel
import java.util.Locale

@Composable
fun ProductsScreen(
    viewModel: ProductViewModel,
    onNavigateToRoute: (String) -> Unit,
    onLogout: () -> Unit
) {
    val produtos by viewModel.produtos.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCatFilter by viewModel.selectedCategoryFilter.collectAsState()
    val isDialogVisible by viewModel.isDialogVisible.collectAsState()
    val editingProduto by viewModel.editingProduto.collectAsState()

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
                title = "Produtos",
                onSettingsClick = { onNavigateToRoute(Screen.Settings.route) },
                onLogoutClick = onLogout
            )
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = Screen.Products.route,
                onNavigateToRoute = onNavigateToRoute
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openDialog() }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Produto")
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
                placeholder = "Buscar produto..."
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCatFilter == null,
                    onClick = { viewModel.onCategoryFilterSelect(null) },
                    label = { Text("Todas") }
                )
                categorias.forEach { cat ->
                    FilterChip(
                        selected = selectedCatFilter == cat.id,
                        onClick = { viewModel.onCategoryFilterSelect(cat.id) },
                        label = { Text(cat.nome) }
                    )
                }
            }

            if (produtos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum produto encontrado.",
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
                    items(produtos, key = { it.id }) { prod ->
                        ProductCard(
                            produto = prod,
                            onEdit = { viewModel.openDialog(prod) },
                            onDelete = { deleteCandidateId = prod.id }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (isDialogVisible) {
        ProductFormDialog(
            produto = editingProduto,
            categorias = categorias,
            onDismiss = { viewModel.closeDialog() },
            onSave = { nome, desc, preco, estoque, catId ->
                viewModel.saveProduto(nome, desc, preco, estoque, catId)
            }
        )
    }

    if (deleteCandidateId != null) {
        DeleteConfirmationDialog(
            title = "Excluir Produto",
            message = "Tem certeza de que deseja excluir este produto?",
            onConfirm = {
                viewModel.deleteProduto(deleteCandidateId!!)
                deleteCandidateId = null
            },
            onDismiss = { deleteCandidateId = null }
        )
    }
}

@Composable
private fun ProductCard(
    produto: Produto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                Text(
                    text = produto.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (produto.categoriaNome.isNotBlank()) {
                    Text(
                        text = "Categoria: ${produto.categoriaNome}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = String.format(Locale.getDefault(), "R$ %.2f", produto.preco),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Estoque: ${produto.estoque}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
private fun ProductFormDialog(
    produto: Produto?,
    categorias: List<Categoria>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Int) -> Unit
) {
    var nome by remember { mutableStateOf(produto?.nome ?: "") }
    var descricao by remember { mutableStateOf(produto?.descricao ?: "") }
    var preco by remember { mutableStateOf(produto?.preco?.toString() ?: "") }
    var estoque by remember { mutableStateOf(produto?.estoque?.toString() ?: "") }
    var selectedCatId by remember { mutableStateOf(produto?.categoriaId ?: categorias.firstOrNull()?.id ?: 0) }
    var expanded by remember { mutableStateOf(false) }

    val selectedCatName = categorias.find { it.id == selectedCatId }?.nome ?: "Selecione uma Categoria"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (produto == null) "Novo Produto" else "Editar Produto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do Produto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = preco,
                    onValueChange = { preco = it },
                    label = { Text("Preço (ex: 99.90)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estoque,
                    onValueChange = { estoque = it },
                    label = { Text("Quantidade em Estoque") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCatName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nome) },
                                onClick = {
                                    selectedCatId = cat.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(nome, descricao, preco, estoque, selectedCatId) }) {
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
