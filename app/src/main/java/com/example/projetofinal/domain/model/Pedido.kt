package com.example.projetofinal.domain.model

enum class StatusPedido {
    PENDENTE,
    CONCLUIDO,
    CANCELADO
}

data class Pedido(
    val id: Int = 0,
    val clienteId: Int,
    val clienteNome: String = "",
    val produtoId: Int,
    val produtoNome: String = "",
    val quantidade: Int,
    val valorTotal: Double,
    val data: String,
    val status: StatusPedido = StatusPedido.PENDENTE
)
