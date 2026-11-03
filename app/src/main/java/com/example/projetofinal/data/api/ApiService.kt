package com.example.projetofinal.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // Autenticação
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Categorias
    @GET("categorias")
    suspend fun getCategorias(): Response<List<CategoriaDto>>

    @POST("categorias")
    suspend fun createCategoria(@Body categoria: CategoriaDto): Response<CategoriaDto>

    @PUT("categorias/{id}")
    suspend fun updateCategoria(@Path("id") id: Int, @Body categoria: CategoriaDto): Response<CategoriaDto>

    @DELETE("categorias/{id}")
    suspend fun deleteCategoria(@Path("id") id: Int): Response<Unit>

    // Produtos
    @GET("produtos")
    suspend fun getProdutos(): Response<List<ProdutoDto>>

    @POST("produtos")
    suspend fun createProduto(@Body produto: ProdutoDto): Response<ProdutoDto>

    @PUT("produtos/{id}")
    suspend fun updateProduto(@Path("id") id: Int, @Body produto: ProdutoDto): Response<ProdutoDto>

    @DELETE("produtos/{id}")
    suspend fun deleteProduto(@Path("id") id: Int): Response<Unit>

    // Clientes
    @GET("clientes")
    suspend fun getClientes(): Response<List<ClienteDto>>

    @POST("clientes")
    suspend fun createCliente(@Body cliente: ClienteDto): Response<ClienteDto>

    @PUT("clientes/{id}")
    suspend fun updateCliente(@Path("id") id: Int, @Body cliente: ClienteDto): Response<ClienteDto>

    @DELETE("clientes/{id}")
    suspend fun deleteCliente(@Path("id") id: Int): Response<Unit>

    // Pedidos
    @GET("pedidos")
    suspend fun getPedidos(): Response<List<PedidoDto>>

    @POST("pedidos")
    suspend fun createPedido(@Body pedido: PedidoDto): Response<PedidoDto>

    @PUT("pedidos/{id}")
    suspend fun updatePedido(@Path("id") id: Int, @Body pedido: PedidoDto): Response<PedidoDto>

    @DELETE("pedidos/{id}")
    suspend fun deletePedido(@Path("id") id: Int): Response<Unit>
}
