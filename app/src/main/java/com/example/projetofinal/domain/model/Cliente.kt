package com.example.projetofinal.domain.model

data class Cliente(
    val id: Int = 0,
    val nome: String,
    val email: String,
    val telefone: String = "",
    val cpf: String = ""
)
