package com.example.projetofinal.data.api

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("usuario") val usuario: String,
    @SerializedName("senha") val senha: String
)

data class LoginResponse(
    @SerializedName("sucesso") val sucesso: Boolean,
    @SerializedName("mensagem") val mensagem: String,
    @SerializedName("usuario") val usuario: String? = null,
    @SerializedName("token") val token: String? = null
)

data class CategoriaDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String? = ""
)

data class ProdutoDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("nome") val nome: String,
    @SerializedName("descricao") val descricao: String? = "",
    @SerializedName("preco") val preco: Double,
    @SerializedName("estoque") val estoque: Int,
    @SerializedName("categoria_id") val categoriaId: Int,
    @SerializedName("categoria_nome") val categoriaNome: String? = ""
)

data class ClienteDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefone") val telefone: String? = "",
    @SerializedName("cpf") val cpf: String? = ""
)

data class PedidoDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("cliente_id") val clienteId: Int,
    @SerializedName("cliente_nome") val clienteNome: String? = "",
    @SerializedName("produto_id") val produtoId: Int,
    @SerializedName("produto_nome") val produtoNome: String? = "",
    @SerializedName("quantidade") val quantidade: Int,
    @SerializedName("valor_total") val valorTotal: Double,
    @SerializedName("data") val data: String,
    @SerializedName("status") val status: String
)
