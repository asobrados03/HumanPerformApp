package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.data.model.UserBooking
import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.shared.domain.usecase.UserUseCase
import com.humanperformcenter.shared.domain.usecase.validation.EditValidationResult
import com.humanperformcenter.shared.domain.usecase.validation.UserValidator
import com.humanperformcenter.ui.viewmodel.state.BlogState
import com.humanperformcenter.ui.viewmodel.state.CoachState
import com.humanperformcenter.ui.viewmodel.state.DeleteUserState
import com.humanperformcenter.ui.viewmodel.state.UpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(
    private val userUseCase: UserUseCase
) : ViewModel() {

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    private val _userBookings = MutableStateFlow<List<UserBooking>>(emptyList())
    val userBookings: StateFlow<List<UserBooking>> get() = _userBookings

    // 2) Flag de carga
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            // Lee sólo UNA vez el user almacenado
            val storedUser = SecureStorage.userFlow().firstOrNull()
            _userData.value = storedUser
            _isLoading.value = false
        }
    }

    // 2) LiveData para exponer el estado de la operación de update
    private val _updateState = MutableLiveData<UpdateState>(UpdateState.Idle)
    val updateState: LiveData<UpdateState> = _updateState

    private val _deleteState = MutableStateFlow<DeleteUserState>(DeleteUserState.Idle)
    val deleteState: StateFlow<DeleteUserState> = _deleteState.asStateFlow()

    private val _coachesState = MutableStateFlow<CoachState>(CoachState.Idle)
    // Estado público inmutable
    val coachesState: StateFlow<CoachState> = _coachesState.asStateFlow()

    val favoriteCoachId: StateFlow<Int?> = SecureStorage.favoriteCoachFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Recibe un User “candidato” (con campos fullName, dateOfBirth = "yyyy-MM-dd",
     * sex, phone, postcode, dni, etc.). Primero lo valida mediante el caso de uso; si hay
     * errores, emite ValidationErrors con el mapa. Si no, emite Loading y llama a updateUser()
     * del caso de uso.
     */
    fun updateUser(candidate: User) {
        // 1) Convertir dateOfBirth de "yyyy-MM-dd" (que la UI ya le pasó) a formato dd/MM/yyyy
        //    para validar en el caso de uso. Podemos invertir la cadena:
        val dobParts = candidate.dateOfBirth.split("-")
        val dateOfBirthText = if (dobParts.size == 3) {
            val y = dobParts[0].padStart(4, '0')
            val m = dobParts[1].padStart(2, '0')
            val d = dobParts[2].padStart(2, '0')
            "$d/$m/$y"
        } else {
            ""
        }

        // 2) Invocar al caso de uso para validar
        val validation = UserValidator.validateProfile(
            fullName = candidate.fullName,
            dateOfBirthText = dateOfBirthText,
            selectedSexBackend = candidate.sex,
            phone = candidate.phone,
            dni = candidate.dni.toString()
        )

        if (validation is EditValidationResult.Error) {
            // 2.1) Si hay errores, los voltamos a UpdateState.ValidationErrors
            val fieldErrors = validation.fieldErrors.mapKeys { (campo, _) ->
                // Mapear ValidationResult.Field → UpdateState.Field
                when (campo) {
                    EditValidationResult.Field.FULL_NAME -> UpdateState.Field.FULL_NAME
                    EditValidationResult.Field.DATE_OF_BIRTH -> UpdateState.Field.DATE_OF_BIRTH
                    EditValidationResult.Field.SEX -> UpdateState.Field.SEX
                    EditValidationResult.Field.PHONE -> UpdateState.Field.PHONE
                    EditValidationResult.Field.DNI -> UpdateState.Field.DNI
                }
            }
            _updateState.value = UpdateState.ValidationErrors(fieldErrors)
            return
        }

        // 3) Si pasó validación, invocamos updateUser() en Loading
        _updateState.value = UpdateState.Loading

        viewModelScope.launch {
            val result = userUseCase.updateUser(candidate)

            result.onSuccess { newUser ->
                _updateState.value = UpdateState.Success(newUser)
            }.onFailure { throwable ->
                _updateState.value =
                    UpdateState.Error(throwable.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Opción para limpiar el estado luego de procesar el resultado en la UI.
     */
    fun clearUpdateState() {
        _updateState.value = UpdateState.Idle
    }

    fun deleteUser(email: String) {
        viewModelScope.launch {
            _deleteState.value = DeleteUserState.Loading

            userUseCase.deleteUser(email).fold(
                onSuccess = {
                    _deleteState.value = DeleteUserState.Success
                },
                onFailure = { throwable ->
                    _deleteState.value = when {
                        // si el mensaje de excepción incluye "no encontrado"
                        throwable.message?.contains("no encontrado", ignoreCase = true) == true ->
                            DeleteUserState.NotFound(email)
                        else ->
                            DeleteUserState.Error(throwable.message ?: "Error desconocido")
                    }
                }
            )
        }
    }

    /** Úsalo si quieres resetear el flujo (por ejemplo al salir de la pantalla) */
    fun resetDeleteState() {
        _deleteState.value = DeleteUserState.Idle
    }

    fun getCoaches() {
        _coachesState.value = CoachState.Loading
        viewModelScope.launch {
            userUseCase.getCoaches().onSuccess { professionals ->
                _coachesState.value = CoachState.Success(professionals)
            }.onFailure { throwable ->
                _coachesState.value = CoachState.Error(
                    throwable.message.orEmpty().ifEmpty { "Error desconocido al cargar blogs" }
                )
            }
        }
    }

    fun markFavorite(id: Int) {
        viewModelScope.launch {
            SecureStorage.saveFavoriteCoach(id)
        }
    }

    fun fetchUserBookings(userId: Int) {
        viewModelScope.launch {
            _userBookings.value = userUseCase.getUserBookings(userId)
        }
    }

    fun cancelUserBooking(bookingId: Int) {
        viewModelScope.launch {
            userUseCase.cancelUserBooking(bookingId).fold(
                onSuccess = {
                    println("Reserva cancelada exitosamente")
                    fetchUserBookings(_userData.value?.id ?: 0)
                },
                onFailure = { throwable ->
                    println("Error al cancelar la reserva: ${throwable.message}")
                }
            )
        }
    }

}
