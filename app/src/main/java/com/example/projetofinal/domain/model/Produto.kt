package com.example.projetofinal.domain.model

data class Produto(
    val id: Int = 0,
    val nome: String,
    val descricao: String = "",
    val preco: Double,
    val estoque: Int,
    val categoriaId: Int,
    val categoriaNome: String = ""
)
