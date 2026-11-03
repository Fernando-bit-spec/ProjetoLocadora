package com.example.projetofinal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetofinal.data.repository.AppRepository
import com.example.projetofinal.domain.model.Categoria
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

class CategoryViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDialogVisible = MutableStateFlow(false)
    val isDialogVisible: StateFlow<Boolean> = _isDialogVisible.asStateFlow()

    private val _editingCategoria = MutableStateFlow<Categoria?>(null)
    val editingCategoria: StateFlow<Categoria?> = _editingCategoria.asStateFlow()

    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    val categorias: StateFlow<List<Categoria>> = combine(
        repository.categoriasFlow,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.nome.contains(query, ignoreCase = true) ||
                    it.descricao.contains(query, ignoreCase = true)
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
            repository.loadCategorias()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun openDialog(categoria: Categoria? = null) {
        _editingCategoria.value = categoria
        _isDialogVisible.value = true
    }

    fun closeDialog() {
        _isDialogVisible.value = false
        _editingCategoria.value = null
    }

    fun saveCategoria(nome: String, descricao: String) {
        if (nome.isBlank()) {
            viewModelScope.launch { _snackBarMessage.emit("O nome da categoria é obrigatório.") }
            return
        }

        viewModelScope.launch {
            val current = _editingCategoria.value
            if (current == null) {
                val newCat = Categoria(nome = nome, descricao = descricao)
                repository.addCategoria(newCat)
                _snackBarMessage.emit("Categoria criada com sucesso!")
            } else {
                val updated = current.copy(nome = nome, descricao = descricao)
                repository.updateCategoria(updated)
                _snackBarMessage.emit("Categoria atualizada com sucesso!")
            }
            closeDialog()
        }
    }

    fun deleteCategoria(id: Int) {
        viewModelScope.launch {
            repository.deleteCategoria(id)
            _snackBarMessage.emit("Categoria removida com sucesso!")
        }
    }
}
