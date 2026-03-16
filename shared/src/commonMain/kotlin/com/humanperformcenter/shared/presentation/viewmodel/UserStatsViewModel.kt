package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.domain.usecase.UserUseCase
import com.humanperformcenter.shared.presentation.ui.UserStatsState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserStatsViewModel(
    private val userUseCase: UserUseCase
) : ViewModel() {
    companion object {
        val log = logging() // Uses class name as tag
    }

    private val _uiState = MutableStateFlow<UserStatsState>(UserStatsState.Loading)
    @NativeCoroutinesState
    val uiState: StateFlow<UserStatsState> = _uiState.asStateFlow()

    fun loadStatistics(userId: Int) {
        log.debug { "📊 Cargando estadísticas para userId: $userId" }

        if (userId <= 0) {
            val message = "ID de usuario inválido"
            log.error { "❌ Error al cargar estadísticas: $message" }
            _uiState.value = UserStatsState.Error(message)
            return
        }

        viewModelScope.launch {
            _uiState.value = UserStatsState.Loading

            userUseCase.getUserStats(userId)
                .onSuccess { stats ->
                    log.info { "✅ Estadísticas cargadas correctamente para userId: $userId" }
                    _uiState.value = UserStatsState.Success(stats)
                }
                .onFailure { exception ->
                    log.error { "❌ Error al cargar estadísticas: ${exception.message}" }
                    _uiState.value = UserStatsState.Error(exception.message ?: "Error desconocido")
                }
        }
    }
}