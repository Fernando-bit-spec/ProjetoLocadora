package com.example.projetofinal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetofinal.data.datastore.UserPreferencesRepository
import com.example.projetofinal.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val selectedTab: Int = 0, // 0: Login, 1: Cadastro
    val username: String = "",
    val senha: String = "",
    val isLoading: Boolean = false,
    val rememberMe: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,

    // Cadastro
    val regUsername: String = "",
    val regEmail: String = "",
    val regSenha: String = "",
    val regConfirmSenha: String = ""
)

class AuthViewModel(
    private val repository: AppRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = prefs.isLoggedIn,
                    username = if (prefs.isLoggedIn) prefs.loggedInUser else _uiState.value.username
                )
            }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index, errorMessage = null)
    }

    fun onUsernameChange(newVal: String) {
        _uiState.value = _uiState.value.copy(username = newVal, errorMessage = null)
    }

    fun onSenhaChange(newVal: String) {
        _uiState.value = _uiState.value.copy(senha = newVal, errorMessage = null)
    }

    fun onRememberMeChange(newVal: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = newVal)
    }

    fun onRegUsernameChange(newVal: String) {
        _uiState.value = _uiState.value.copy(regUsername = newVal, errorMessage = null)
    }

    fun onRegEmailChange(newVal: String) {
        _uiState.value = _uiState.value.copy(regEmail = newVal, errorMessage = null)
    }

    fun onRegSenhaChange(newVal: String) {
        _uiState.value = _uiState.value.copy(regSenha = newVal, errorMessage = null)
    }

    fun onRegConfirmSenhaChange(newVal: String) {
        _uiState.value = _uiState.value.copy(regConfirmSenha = newVal, errorMessage = null)
    }

    fun login() {
        val current = _uiState.value
        if (current.username.isBlank()) {
            _uiState.value = current.copy(errorMessage = "Por favor, informe o usuário.")
            return
        }
        if (current.senha.isBlank()) {
            _uiState.value = current.copy(errorMessage = "Por favor, informe a senha.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.login(current.username, current.senha)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess { user ->
                preferencesRepository.setLoggedIn(true, user.username)
                _snackBarMessage.emit("Bem-vindo(a), ${user.username}!")
            }.onFailure { ex ->
                val msg = ex.message ?: "Erro ao realizar login."
                _uiState.value = _uiState.value.copy(errorMessage = msg)
                _snackBarMessage.emit(msg)
            }
        }
    }

    fun register() {
        val current = _uiState.value
        if (current.regUsername.isBlank()) {
            _uiState.value = current.copy(errorMessage = "Por favor, informe o usuário.")
            return
        }
        if (current.regEmail.isBlank() || !current.regEmail.contains("@")) {
            _uiState.value = current.copy(errorMessage = "Por favor, informe um e-mail válido.")
            return
        }
        if (current.regSenha.isBlank()) {
            _uiState.value = current.copy(errorMessage = "Por favor, informe a senha.")
            return
        }
        if (current.regSenha.length < 4) {
            _uiState.value = current.copy(errorMessage = "A senha deve ter pelo menos 4 caracteres.")
            return
        }
        if (current.regSenha != current.regConfirmSenha) {
            _uiState.value = current.copy(errorMessage = "As senhas não coincidem.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.register(current.regUsername, current.regEmail, current.regSenha)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess { user ->
                preferencesRepository.setLoggedIn(true, user.username)
                _snackBarMessage.emit("Cadastro realizado! Bem-vindo(a), ${user.username}!")
            }.onFailure { ex ->
                val msg = ex.message ?: "Erro ao realizar cadastro."
                _uiState.value = _uiState.value.copy(errorMessage = msg)
                _snackBarMessage.emit(msg)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferencesRepository.setLoggedIn(false, "")
            _uiState.value = AuthUiState()
            _snackBarMessage.emit("Sessão encerrada.")
        }
    }
}
