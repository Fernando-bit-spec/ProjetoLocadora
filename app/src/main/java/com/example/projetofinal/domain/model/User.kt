package com.example.projetofinal.domain.model

data class User(
    val id: Int = 0,
    val username: String,
    val email: String,
    val token: String? = null
)
