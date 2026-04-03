package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.domain.usecase.UserStatsUseCase
import com.humanperformcenter.shared.presentation.ui.UserStatsState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserStatsViewModel(
    private val userStatsUseCase: UserStatsUseCase
) : ViewModel() {
    companion object {
        val log = logging() // Uses class name as tag
    }

    private val _uiState = MutableStateFlow<UserStatsState>(viewModelScope, UserStatsState.Loading)
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

            userStatsUseCase.getUserStats(userId)
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