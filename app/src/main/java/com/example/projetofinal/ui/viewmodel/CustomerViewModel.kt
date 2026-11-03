package com.example.projetofinal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetofinal.data.repository.AppRepository
import com.example.projetofinal.domain.model.Cliente
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

class CustomerViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDialogVisible = MutableStateFlow(false)
    val isDialogVisible: StateFlow<Boolean> = _isDialogVisible.asStateFlow()

    private val _editingCliente = MutableStateFlow<Cliente?>(null)
    val editingCliente: StateFlow<Cliente?> = _editingCliente.asStateFlow()

    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    val clientes: StateFlow<List<Cliente>> = combine(
        repository.clientesFlow,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.nome.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true) ||
                    it.cpf.contains(query, ignoreCase = true)
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
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun openDialog(cliente: Cliente? = null) {
        _editingCliente.value = cliente
        _isDialogVisible.value = true
    }

    fun closeDialog() {
        _isDialogVisible.value = false
        _editingCliente.value = null
    }

    fun saveCliente(
        nome: String,
        email: String,
        telefone: String,
        cpf: String
    ) {
        if (nome.isBlank()) {
            viewModelScope.launch { _snackBarMessage.emit("Nome do cliente é obrigatório.") }
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            viewModelScope.launch { _snackBarMessage.emit("E-mail do cliente inválido.") }
            return
        }

        viewModelScope.launch {
            val current = _editingCliente.value
            if (current == null) {
                val newCli = Cliente(nome = nome, email = email, telefone = telefone, cpf = cpf)
                repository.addCliente(newCli)
                _snackBarMessage.emit("Cliente cadastrado com sucesso!")
            } else {
                val updated = current.copy(nome = nome, email = email, telefone = telefone, cpf = cpf)
                repository.updateCliente(updated)
                _snackBarMessage.emit("Cliente atualizado com sucesso!")
            }
            closeDialog()
        }
    }

    fun deleteCliente(id: Int) {
        viewModelScope.launch {
            repository.deleteCliente(id)
            _snackBarMessage.emit("Cliente excluído com sucesso!")
        }
    }
}
