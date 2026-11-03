package com.example.projetofinal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetofinal.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardSummary(
    val totalCategorias: Int = 0,
    val totalProdutos: Int = 0,
    val totalClientes: Int = 0,
    val totalPedidos: Int = 0,
    val faturamentoTotal: Double = 0.0,
    val pedidosPendentes: Int = 0
)

class MainViewModel(
    private val repository: AppRepository
) : ViewModel() {

    val summary: StateFlow<DashboardSummary> = combine(
        repository.categoriasFlow,
        repository.produtosFlow,
        repository.clientesFlow,
        repository.pedidosFlow
    ) { categorias, produtos, clientes, pedidos ->
        DashboardSummary(
            totalCategorias = categorias.size,
            totalProdutos = produtos.size,
            totalClientes = clientes.size,
            totalPedidos = pedidos.size,
            faturamentoTotal = pedidos.sumOf { it.valorTotal },
            pedidosPendentes = pedidos.count { it.status.name == "PENDENTE" }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardSummary()
    )

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            repository.loadCategorias()
            repository.loadProdutos()
            repository.loadClientes()
            repository.loadPedidos()
        }
    }
}
