package com.example.projetofinal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetofinal.data.repository.AppRepository
import com.example.projetofinal.domain.model.Categoria
import com.example.projetofinal.domain.model.Produto
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

class ProductViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDialogVisible = MutableStateFlow(false)
    val isDialogVisible: StateFlow<Boolean> = _isDialogVisible.asStateFlow()

    private val _editingProduto = MutableStateFlow<Produto?>(null)
    val editingProduto: StateFlow<Produto?> = _editingProduto.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<Int?>(null)
    val selectedCategoryFilter: StateFlow<Int?> = _selectedCategoryFilter.asStateFlow()

    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    val categorias: StateFlow<List<Categoria>> = repository.categoriasFlow

    val produtos: StateFlow<List<Produto>> = combine(
        repository.produtosFlow,
        _searchQuery,
        _selectedCategoryFilter
    ) { list, query, catFilter ->
        list.filter { prod ->
            val matchesQuery = query.isBlank() ||
                    prod.nome.contains(query, ignoreCase = true) ||
                    prod.descricao.contains(query, ignoreCase = true)
            val matchesCat = catFilter == null || prod.categoriaId == catFilter
            matchesQuery && matchesCat
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
            repository.loadProdutos()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryFilterSelect(catId: Int?) {
        _selectedCategoryFilter.value = catId
    }

    fun openDialog(produto: Produto? = null) {
        _editingProduto.value = produto
        _isDialogVisible.value = true
    }

    fun closeDialog() {
        _isDialogVisible.value = false
        _editingProduto.value = null
    }

    fun saveProduto(
        nome: String,
        descricao: String,
        precoStr: String,
        estoqueStr: String,
        categoriaId: Int
    ) {
        if (nome.isBlank()) {
            viewModelScope.launch { _snackBarMessage.emit("Informe o nome do produto.") }
            return
        }
        val preco = precoStr.replace(",", ".").toDoubleOrNull()
        if (preco == null || preco < 0) {
            viewModelScope.launch { _snackBarMessage.emit("Preço inválido.") }
            return
        }
        val estoque = estoqueStr.toIntOrNull()
        if (estoque == null || estoque < 0) {
            viewModelScope.launch { _snackBarMessage.emit("Quantidade em estoque inválida.") }
            return
        }
        if (categoriaId <= 0) {
            viewModelScope.launch { _snackBarMessage.emit("Selecione uma categoria.") }
            return
        }

        viewModelScope.launch {
            val cat = repository.categoriasFlow.value.find { it.id == categoriaId }
            val catNome = cat?.nome ?: "Sem Categoria"
            val current = _editingProduto.value

            if (current == null) {
                val newProd = Produto(
                    nome = nome,
                    descricao = descricao,
                    preco = preco,
                    estoque = estoque,
                    categoriaId = categoriaId,
                    categoriaNome = catNome
                )
                repository.addProduto(newProd)
                _snackBarMessage.emit("Produto cadastrado com sucesso!")
            } else {
                val updated = current.copy(
                    nome = nome,
                    descricao = descricao,
                    preco = preco,
                    estoque = estoque,
                    categoriaId = categoriaId,
                    categoriaNome = catNome
                )
                repository.updateProduto(updated)
                _snackBarMessage.emit("Produto atualizado com sucesso!")
            }
            closeDialog()
        }
    }

    fun deleteProduto(id: Int) {
        viewModelScope.launch {
            repository.deleteProduto(id)
            _snackBarMessage.emit("Produto excluído com sucesso!")
        }
    }
}
