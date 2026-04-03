package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.domain.usecase.UserCoachesUseCase
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.presentation.ui.CoachState
import com.humanperformcenter.shared.presentation.ui.GetPreferredCoachState
import com.humanperformcenter.shared.presentation.ui.MarkFavoriteState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserFavoritesViewModel(
    private val userCoachesUseCase: UserCoachesUseCase
) : ViewModel() {
    private val _coachesState = MutableStateFlow<CoachState>(viewModelScope, CoachState.Idle)
    @NativeCoroutinesState
    val coachesState: StateFlow<CoachState> = _coachesState

    private val _markFavoriteState = MutableStateFlow<MarkFavoriteState>(viewModelScope, MarkFavoriteState.Idle)
    @NativeCoroutinesState
    val markFavoriteState: StateFlow<MarkFavoriteState> = _markFavoriteState

    private val _getPreferredCoachState = MutableStateFlow<GetPreferredCoachState>(viewModelScope, GetPreferredCoachState.Idle)
    @NativeCoroutinesState
    val getPreferredCoachState: StateFlow<GetPreferredCoachState> = _getPreferredCoachState

    fun getCoaches() {
        _coachesState.value = CoachState.Loading
        viewModelScope.launch {
            userCoachesUseCase.getCoaches().onSuccess { professionals ->
                _coachesState.value = CoachState.Success(professionals)
            }.onFailure { throwable ->
                _coachesState.value = CoachState.Error(
                    throwable.message.orEmpty().ifEmpty {
                        "Error desconocido al cargar profesionales"
                    }
                )
            }
        }
    }

    fun markFavorite(coachId: Int, serviceName: String?, userId: Int?) {
        _markFavoriteState.value = MarkFavoriteState.Loading
        viewModelScope.launch {
            userCoachesUseCase.markFavorite(coachId, serviceName, userId).onSuccess { message ->
                _markFavoriteState.value = MarkFavoriteState.Success(message)
            }.onFailure { throwable ->
                _markFavoriteState.value = MarkFavoriteState.Error(
                    throwable.message.orEmpty().ifEmpty {
                        "Error desconocido al marcar como favorito"
                    }
                )
            }
        }
    }

    fun clearMarkFavoriteState() {
        _markFavoriteState.value = MarkFavoriteState.Idle
    }

    fun getPreferredCoach(userId: Int?) {
        if (userId == null) return
        _getPreferredCoachState.value = GetPreferredCoachState.Loading
        viewModelScope.launch {
            try {
                userCoachesUseCase.getPreferredCoach(customerId = userId).onSuccess { preferred ->
                    _getPreferredCoachState.value = GetPreferredCoachState.Success(preferred.coachId)
                }.onFailure { throwable ->
                    _getPreferredCoachState.value = GetPreferredCoachState.Error(
                        throwable.message ?: "Error al obtener favorito"
                    )
                }
            } catch (e: Throwable) {
                _getPreferredCoachState.value = GetPreferredCoachState.Error(
                    e.message ?: "Error al obtener favorito"
                )
            }
        }
    }

    fun clearGetPreferredCoachState() {
        _getPreferredCoachState.value = GetPreferredCoachState.Idle
    }

    fun coachesStateKind(): String = when (_coachesState.value) {
        CoachState.Idle -> "idle"
        CoachState.Loading -> "loading"
        is CoachState.Success -> "success"
        is CoachState.Error -> "error"
    }

    fun coachesStateMessage(): String? = when (val state = _coachesState.value) {
        is CoachState.Error -> state.message
        else -> null
    }

    fun coachesList(): List<Professional> = when (val state = _coachesState.value) {
        is CoachState.Success -> state.coaches
        else -> emptyList()
    }

    fun markFavoriteStateKind(): String = when (_markFavoriteState.value) {
        MarkFavoriteState.Idle -> "idle"
        MarkFavoriteState.Loading -> "loading"
        is MarkFavoriteState.Success -> "success"
        is MarkFavoriteState.Error -> "error"
    }

    fun markFavoriteStateMessage(): String? = when (val state = _markFavoriteState.value) {
        is MarkFavoriteState.Success -> state.message
        is MarkFavoriteState.Error -> state.message
        else -> null
    }

    fun preferredCoachStateKind(): String = when (_getPreferredCoachState.value) {
        GetPreferredCoachState.Idle -> "idle"
        GetPreferredCoachState.Loading -> "loading"
        is GetPreferredCoachState.Success -> "success"
        is GetPreferredCoachState.Error -> "error"
    }

    fun preferredCoachStateMessage(): String? = when (val state = _getPreferredCoachState.value) {
        is GetPreferredCoachState.Error -> state.message
        else -> null
    }

    fun preferredCoachId(): Int? = when (val state = _getPreferredCoachState.value) {
        is GetPreferredCoachState.Success -> state.coachId
        else -> null
    }
}
