package com.example.projetofinal.data.repository

import com.example.projetofinal.data.api.ApiClient
import com.example.projetofinal.data.api.ApiService
import com.example.projetofinal.data.api.CategoriaDto
import com.example.projetofinal.data.api.ClienteDto
import com.example.projetofinal.data.api.LoginRequest
import com.example.projetofinal.data.api.PedidoDto
import com.example.projetofinal.data.api.ProdutoDto
import com.example.projetofinal.domain.model.Categoria
import com.example.projetofinal.domain.model.Cliente
import com.example.projetofinal.domain.model.Pedido
import com.example.projetofinal.domain.model.Produto
import com.example.projetofinal.domain.model.StatusPedido
import com.example.projetofinal.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AppRepository(
    private var apiService: ApiService = ApiClient.getService()
) {

    // Initial Mock Seed Data (so the app works immediately even if remote MySQL server is offline)
    private val mockCategorias = mutableListOf(
        Categoria(1, "Eletrônicos", "Aparelhos, celulares e gadgets"),
        Categoria(2, "Móveis", "Móveis para escritório e casa"),
        Categoria(3, "Informática", "Computadores, periféricos e acessórios"),
        Categoria(4, "Papelaria", "Material de escritório e escolar")
    )

    private val mockProdutos = mutableListOf(
        Produto(1, "Smartphone Galaxy A54", "Celular 128GB Tela 6.4", 1899.90, 15, 1, "Eletrônicos"),
        Produto(2, "Cadeira Ergonômica", "Cadeira de escritório ajustável", 850.00, 8, 2, "Móveis"),
        Produto(3, "Notebook Pro 15", "Notebook Intel i7 16GB RAM SSD 512GB", 4500.00, 5, 3, "Informática"),
        Produto(4, "Mouse Sem Fio RGB", "Mouse ergonômico 16000 DPI", 129.90, 30, 3, "Informática"),
        Produto(5, "Caderno Universitário", "Caderno 10 matérias 200 folhas", 29.90, 50, 4, "Papelaria")
    )

    private val mockClientes = mutableListOf(
        Cliente(1, "Ana Silva", "ana.silva@email.com", "(11) 98765-4321", "123.456.789-00"),
        Cliente(2, "Carlos Oliveira", "carlos.o@email.com", "(19) 97123-8899", "987.654.321-11"),
        Cliente(3, "Beatriz Souza", "beatriz.souza@email.com", "(16) 99887-1122", "456.789.123-22"),
        Cliente(4, "Diego Santos", "diego.santos@email.com", "(11) 96543-2211", "321.654.987-33")
    )

    private val mockPedidos = mutableListOf(
        Pedido(1, 1, "Ana Silva", 1, "Smartphone Galaxy A54", 1, 1899.90, "2026-03-01", StatusPedido.CONCLUIDO),
        Pedido(2, 2, "Carlos Oliveira", 2, "Cadeira Ergonômica", 2, 1700.00, "2026-03-02", StatusPedido.PENDENTE),
        Pedido(3, 3, "Beatriz Souza", 4, "Mouse Sem Fio RGB", 1, 129.90, "2026-03-03", StatusPedido.CONCLUIDO)
    )

    private val _categoriasFlow = MutableStateFlow<List<Categoria>>(mockCategorias.toList())
    val categoriasFlow: StateFlow<List<Categoria>> = _categoriasFlow.asStateFlow()

    private val _produtosFlow = MutableStateFlow<List<Produto>>(mockProdutos.toList())
    val produtosFlow: StateFlow<List<Produto>> = _produtosFlow.asStateFlow()

    private val _clientesFlow = MutableStateFlow<List<Cliente>>(mockClientes.toList())
    val clientesFlow: StateFlow<List<Cliente>> = _clientesFlow.asStateFlow()

    private val _pedidosFlow = MutableStateFlow<List<Pedido>>(mockPedidos.toList())
    val pedidosFlow: StateFlow<List<Pedido>> = _pedidosFlow.asStateFlow()

    // Realizar Login
    suspend fun login(usuario: String, senha: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(usuario, senha))
            if (response.isSuccessful && response.body()?.sucesso == true) {
                val body = response.body()!!
                Result.success(User(1, body.usuario ?: usuario, "admin@email.com", body.token))
            } else {
                // Mock Auth fallback for testing (Accept "admin" / "1234" or any non-empty input)
                if ((usuario == "admin" && senha == "1234") || usuario.isNotBlank() && senha.length >= 4) {
                    Result.success(User(1, usuario, "$usuario@email.com", "token_mock_12345"))
                } else {
                    Result.failure(Exception("Usuário ou senha inválidos"))
                }
            }
        } catch (e: Exception) {
            // Network failure fallback for local demonstration
            if ((usuario == "admin" && senha == "1234") || (usuario.isNotBlank() && senha.length >= 4)) {
                Result.success(User(1, usuario, "$usuario@email.com", "token_mock_12345"))
            } else {
                Result.failure(Exception("Credenciais incorretas ou servidor indisponível."))
            }
        }
    }

    // --- CATEGORIAS ---
    suspend fun loadCategorias(): List<Categoria> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCategorias()
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.map { it.toDomain() }
                mockCategorias.clear()
                mockCategorias.addAll(items)
                _categoriasFlow.value = items
                return@withContext items
            }
        } catch (_: Exception) { }
        _categoriasFlow.value = mockCategorias.toList()
        mockCategorias.toList()
    }

    suspend fun addCategoria(categoria: Categoria): Result<Categoria> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createCategoria(categoria.toDto())
            if (response.isSuccessful && response.body() != null) {
                val newItem = response.body()!!.toDomain()
                mockCategorias.add(newItem)
                _categoriasFlow.value = mockCategorias.toList()
                return@withContext Result.success(newItem)
            }
        } catch (_: Exception) { }

        val newId = (mockCategorias.maxOfOrNull { it.id } ?: 0) + 1
        val newItem = categoria.copy(id = newId)
        mockCategorias.add(newItem)
        _categoriasFlow.value = mockCategorias.toList()
        Result.success(newItem)
    }

    suspend fun updateCategoria(categoria: Categoria): Result<Categoria> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateCategoria(categoria.id, categoria.toDto())
            if (response.isSuccessful && response.body() != null) {
                val updated = response.body()!!.toDomain()
                val index = mockCategorias.indexOfFirst { it.id == categoria.id }
                if (index != -1) mockCategorias[index] = updated
                _categoriasFlow.value = mockCategorias.toList()
                return@withContext Result.success(updated)
            }
        } catch (_: Exception) { }

        val index = mockCategorias.indexOfFirst { it.id == categoria.id }
        if (index != -1) {
            mockCategorias[index] = categoria
            _categoriasFlow.value = mockCategorias.toList()
        }
        Result.success(categoria)
    }

    suspend fun deleteCategoria(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.deleteCategoria(id)
        } catch (_: Exception) { }

        mockCategorias.removeAll { it.id == id }
        _categoriasFlow.value = mockCategorias.toList()
        Result.success(Unit)
    }

    // --- PRODUTOS ---
    suspend fun loadProdutos(): List<Produto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProdutos()
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.map { it.toDomain() }
                mockProdutos.clear()
                mockProdutos.addAll(items)
                _produtosFlow.value = items
                return@withContext items
            }
        } catch (_: Exception) { }
        _produtosFlow.value = mockProdutos.toList()
        mockProdutos.toList()
    }

    suspend fun addProduto(produto: Produto): Result<Produto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createProduto(produto.toDto())
            if (response.isSuccessful && response.body() != null) {
                val newItem = response.body()!!.toDomain()
                mockProdutos.add(newItem)
                _produtosFlow.value = mockProdutos.toList()
                return@withContext Result.success(newItem)
            }
        } catch (_: Exception) { }

        val catNome = mockCategorias.find { it.id == produto.categoriaId }?.nome ?: "Sem Categoria"
        val newId = (mockProdutos.maxOfOrNull { it.id } ?: 0) + 1
        val newItem = produto.copy(id = newId, categoriaNome = catNome)
        mockProdutos.add(newItem)
        _produtosFlow.value = mockProdutos.toList()
        Result.success(newItem)
    }

    suspend fun updateProduto(produto: Produto): Result<Produto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateProduto(produto.id, produto.toDto())
            if (response.isSuccessful && response.body() != null) {
                val updated = response.body()!!.toDomain()
                val index = mockProdutos.indexOfFirst { it.id == produto.id }
                if (index != -1) mockProdutos[index] = updated
                _produtosFlow.value = mockProdutos.toList()
                return@withContext Result.success(updated)
            }
        } catch (_: Exception) { }

        val catNome = mockCategorias.find { it.id == produto.categoriaId }?.nome ?: produto.categoriaNome
        val updated = produto.copy(categoriaNome = catNome)
        val index = mockProdutos.indexOfFirst { it.id == produto.id }
        if (index != -1) {
            mockProdutos[index] = updated
            _produtosFlow.value = mockProdutos.toList()
        }
        Result.success(updated)
    }

    suspend fun deleteProduto(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.deleteProduto(id)
        } catch (_: Exception) { }

        mockProdutos.removeAll { it.id == id }
        _produtosFlow.value = mockProdutos.toList()
        Result.success(Unit)
    }

    // --- CLIENTES ---
    suspend fun loadClientes(): List<Cliente> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getClientes()
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.map { it.toDomain() }
                mockClientes.clear()
                mockClientes.addAll(items)
                _clientesFlow.value = items
                return@withContext items
            }
        } catch (_: Exception) { }
        _clientesFlow.value = mockClientes.toList()
        mockClientes.toList()
    }

    suspend fun addCliente(cliente: Cliente): Result<Cliente> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createCliente(cliente.toDto())
            if (response.isSuccessful && response.body() != null) {
                val newItem = response.body()!!.toDomain()
                mockClientes.add(newItem)
                _clientesFlow.value = mockClientes.toList()
                return@withContext Result.success(newItem)
            }
        } catch (_: Exception) { }

        val newId = (mockClientes.maxOfOrNull { it.id } ?: 0) + 1
        val newItem = cliente.copy(id = newId)
        mockClientes.add(newItem)
        _clientesFlow.value = mockClientes.toList()
        Result.success(newItem)
    }

    suspend fun updateCliente(cliente: Cliente): Result<Cliente> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateCliente(cliente.id, cliente.toDto())
            if (response.isSuccessful && response.body() != null) {
                val updated = response.body()!!.toDomain()
                val index = mockClientes.indexOfFirst { it.id == cliente.id }
                if (index != -1) mockClientes[index] = updated
                _clientesFlow.value = mockClientes.toList()
                return@withContext Result.success(updated)
            }
        } catch (_: Exception) { }

        val index = mockClientes.indexOfFirst { it.id == cliente.id }
        if (index != -1) {
            mockClientes[index] = cliente
            _clientesFlow.value = mockClientes.toList()
        }
        Result.success(cliente)
    }

    suspend fun deleteCliente(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.deleteCliente(id)
        } catch (_: Exception) { }

        mockClientes.removeAll { it.id == id }
        _clientesFlow.value = mockClientes.toList()
        Result.success(Unit)
    }

    // --- PEDIDOS ---
    suspend fun loadPedidos(): List<Pedido> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPedidos()
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.map { it.toDomain() }
                mockPedidos.clear()
                mockPedidos.addAll(items)
                _pedidosFlow.value = items
                return@withContext items
            }
        } catch (_: Exception) { }
        _pedidosFlow.value = mockPedidos.toList()
        mockPedidos.toList()
    }

    suspend fun addPedido(pedido: Pedido): Result<Pedido> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createPedido(pedido.toDto())
            if (response.isSuccessful && response.body() != null) {
                val newItem = response.body()!!.toDomain()
                mockPedidos.add(newItem)
                _pedidosFlow.value = mockPedidos.toList()
                return@withContext Result.success(newItem)
            }
        } catch (_: Exception) { }

        val clienteNome = mockClientes.find { it.id == pedido.clienteId }?.nome ?: "Cliente Exemplo"
        val prod = mockProdutos.find { it.id == pedido.produtoId }
        val prodNome = prod?.nome ?: "Produto Exemplo"
        val valor = prod?.preco?.times(pedido.quantidade) ?: pedido.valorTotal

        val newId = (mockPedidos.maxOfOrNull { it.id } ?: 0) + 1
        val newItem = pedido.copy(
            id = newId,
            clienteNome = clienteNome,
            produtoNome = prodNome,
            valorTotal = if (valor > 0) valor else 100.0
        )
        mockPedidos.add(newItem)
        _pedidosFlow.value = mockPedidos.toList()
        Result.success(newItem)
    }

    suspend fun updatePedido(pedido: Pedido): Result<Pedido> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updatePedido(pedido.id, pedido.toDto())
            if (response.isSuccessful && response.body() != null) {
                val updated = response.body()!!.toDomain()
                val index = mockPedidos.indexOfFirst { it.id == pedido.id }
                if (index != -1) mockPedidos[index] = updated
                _pedidosFlow.value = mockPedidos.toList()
                return@withContext Result.success(updated)
            }
        } catch (_: Exception) { }

        val clienteNome = mockClientes.find { it.id == pedido.clienteId }?.nome ?: pedido.clienteNome
        val prod = mockProdutos.find { it.id == pedido.produtoId }
        val prodNome = prod?.nome ?: pedido.produtoNome
        val updated = pedido.copy(clienteNome = clienteNome, produtoNome = prodNome)

        val index = mockPedidos.indexOfFirst { it.id == pedido.id }
        if (index != -1) {
            mockPedidos[index] = updated
            _pedidosFlow.value = mockPedidos.toList()
        }
        Result.success(updated)
    }

    suspend fun deletePedido(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.deletePedido(id)
        } catch (_: Exception) { }

        mockPedidos.removeAll { it.id == id }
        _pedidosFlow.value = mockPedidos.toList()
        Result.success(Unit)
    }

    // Mappers
    private fun CategoriaDto.toDomain() = Categoria(id, nome, descricao ?: "")
    private fun Categoria.toDto() = CategoriaDto(id, nome, descricao)

    private fun ProdutoDto.toDomain() = Produto(id, nome, descricao ?: "", preco, estoque, categoriaId, categoriaNome ?: "")
    private fun Produto.toDto() = ProdutoDto(id, nome, descricao, preco, estoque, categoriaId, categoriaNome)

    private fun ClienteDto.toDomain() = Cliente(id, nome, email, telefone ?: "", cpf ?: "")
    private fun Cliente.toDto() = ClienteDto(id, nome, email, telefone, cpf)

    private fun PedidoDto.toDomain() = Pedido(
        id = id,
        clienteId = clienteId,
        clienteNome = clienteNome ?: "",
        produtoId = produtoId,
        produtoNome = produtoNome ?: "",
        quantidade = quantidade,
        valorTotal = valorTotal,
        data = data,
        status = when (status.uppercase()) {
            "CONCLUIDO" -> StatusPedido.CONCLUIDO
            "CANCELADO" -> StatusPedido.CANCELADO
            else -> StatusPedido.PENDENTE
        }
    )
    private fun Pedido.toDto() = PedidoDto(
        id = id,
        clienteId = clienteId,
        clienteNome = clienteNome,
        produtoId = produtoId,
        produtoNome = produtoNome,
        quantidade = quantidade,
        valorTotal = valorTotal,
        data = data,
        status = status.name
    )
}
