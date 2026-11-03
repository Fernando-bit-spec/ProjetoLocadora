package com.example.projetofinal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetofinal.data.repository.AppRepository
import com.example.projetofinal.domain.model.Cliente
import com.example.projetofinal.domain.model.Pedido
import com.example.projetofinal.domain.model.Produto
import com.example.projetofinal.domain.model.StatusPedido
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDialogVisible = MutableStateFlow(false)
    val isDialogVisible: StateFlow<Boolean> = _isDialogVisible.asStateFlow()

    private val _editingPedido = MutableStateFlow<Pedido?>(null)
    val editingPedido: StateFlow<Pedido?> = _editingPedido.asStateFlow()

    private val _statusFilter = MutableStateFlow<StatusPedido?>(null)
    val statusFilter: StateFlow<StatusPedido?> = _statusFilter.asStateFlow()

    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    val clientes: StateFlow<List<Cliente>> = repository.clientesFlow
    val produtos: StateFlow<List<Produto>> = repository.produtosFlow

    val pedidos: StateFlow<List<Pedido>> = combine(
        repository.pedidosFlow,
        _searchQuery,
        _statusFilter
    ) { list, query, filter ->
        list.filter { ped ->
            val matchesQuery = query.isBlank() ||
                    ped.clienteNome.contains(query, ignoreCase = true) ||
                    ped.produtoNome.contains(query, ignoreCase = true)
            val matchesFilter = filter == null || ped.status == filter
            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            repository.loadClientes()
            repository.loadProdutos()
            repository.loadPedidos()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterSelect(status: StatusPedido?) {
        _statusFilter.value = status
    }

    fun openDialog(pedido: Pedido? = null) {
        _editingPedido.value = pedido
        _isDialogVisible.value = true
    }

    fun closeDialog() {
        _isDialogVisible.value = false
        _editingPedido.value = null
    }

    fun savePedido(
        clienteId: Int,
        produtoId: Int,
        quantidadeStr: String,
        status: StatusPedido
    ) {
        if (clienteId <= 0) {
            viewModelScope.launch { _snackBarMessage.emit("Selecione um cliente.") }
            return
        }
        if (produtoId <= 0) {
            viewModelScope.launch { _snackBarMessage.emit("Selecione um produto.") }
            return
        }
        val qtd = quantidadeStr.toIntOrNull()
        if (qtd == null || qtd <= 0) {
            viewModelScope.launch { _snackBarMessage.emit("Quantidade inválida.") }
            return
        }

        viewModelScope.launch {
            val cliente = repository.clientesFlow.value.find { it.id == clienteId }
            val produto = repository.produtosFlow.value.find { it.id == produtoId }

            val clienteNome = cliente?.nome ?: "Cliente Exemplo"
            val produtoNome = produto?.nome ?: "Produto Exemplo"
            val total = (produto?.preco ?: 0.0) * qtd
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val current = _editingPedido.value
            if (current == null) {
                val newPed = Pedido(
                    clienteId = clienteId,
                    clienteNome = clienteNome,
                    produtoId = produtoId,
                    produtoNome = produtoNome,
                    quantidade = qtd,
                    valorTotal = total,
                    data = currentDate,
                    status = status
                )
                repository.addPedido(newPed)
                _snackBarMessage.emit("Pedido criado com sucesso!")
            } else {
                val updated = current.copy(
                    clienteId = clienteId,
                    clienteNome = clienteNome,
                    produtoId = produtoId,
                    produtoNome = produtoNome,
                    quantidade = qtd,
                    valorTotal = total,
                    status = status
                )
                repository.updatePedido(updated)
                _snackBarMessage.emit("Pedido atualizado com sucesso!")
            }
            closeDialog()
        }
    }

    fun deletePedido(id: Int) {
        viewModelScope.launch {
            repository.deletePedido(id)
            _snackBarMessage.emit("Pedido excluído com sucesso!")
        }
    }
}
