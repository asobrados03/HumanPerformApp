package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.SessionNotificationManager
import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.shared.domain.usecase.UserUseCase
import com.humanperformcenter.shared.domain.usecase.validation.EditValidationResult
import com.humanperformcenter.shared.domain.usecase.validation.UserValidator
import com.humanperformcenter.shared.presentation.ui.CoachState
import com.humanperformcenter.shared.presentation.ui.CouponUiState
import com.humanperformcenter.shared.presentation.ui.DeleteProfilePicState
import com.humanperformcenter.shared.presentation.ui.DeleteUserState
import com.humanperformcenter.shared.presentation.ui.EwalletUiState
import com.humanperformcenter.shared.presentation.ui.FetchUserBookingsState
import com.humanperformcenter.shared.presentation.ui.GetPreferredCoachState
import com.humanperformcenter.shared.presentation.ui.MarkFavoriteState
import com.humanperformcenter.shared.presentation.ui.UpdateState
import com.humanperformcenter.shared.presentation.ui.UploadState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class UserViewModel(
    private val userUseCase: UserUseCase,
    private val notificationManager: SessionNotificationManager
) : ViewModel() {
    companion object {
        val log = logging() // Uses class name as tag
    }

    val isLoggedInFlow: Flow<Boolean> = SecureStorage.accessTokenFlow()
        .map { token -> token.isNotBlank() }
        .distinctUntilChanged()

    private val _userData = MutableStateFlow<User?>(null)
    @NativeCoroutinesState
    val userData: StateFlow<User?> = _userData

    private val _userBookings = MutableStateFlow<FetchUserBookingsState>(FetchUserBookingsState.Loading)
    @NativeCoroutinesState
    val userBookings: StateFlow<FetchUserBookingsState> = _userBookings.asStateFlow()

    private val _couponUiState = MutableStateFlow(CouponUiState())
    @NativeCoroutinesState
    val couponUiState: StateFlow<CouponUiState> = _couponUiState.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    @NativeCoroutinesState
    val uploadState: StateFlow<UploadState> = _uploadState

    // 2) Flag de carga
    private val _isLoading = MutableStateFlow(true)
    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            SecureStorage.userFlow().collect { storedUser ->
                _userData.value = storedUser
                _isLoading.value = false
            }
        }
    }

    // 2) StateFlow para exponer el estado de la operación de update
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    @NativeCoroutinesState
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteUserState>(DeleteUserState.Idle)
    @NativeCoroutinesState
    val deleteState: StateFlow<DeleteUserState> = _deleteState

    private val _isLoggingOut = MutableStateFlow(false)
    @NativeCoroutinesState
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    private val _coachesState = MutableStateFlow<CoachState>(CoachState.Idle)
    @NativeCoroutinesState
    val coachesState: StateFlow<CoachState> = _coachesState

    private val _markFavoriteState = MutableStateFlow<MarkFavoriteState>(MarkFavoriteState.Idle)
    @NativeCoroutinesState
    val markFavoriteState: StateFlow<MarkFavoriteState> = _markFavoriteState

    private val _deleteProfilePicState = MutableStateFlow<DeleteProfilePicState>(
        DeleteProfilePicState.Idle)
    @NativeCoroutinesState
    val deleteProfilePicState: StateFlow<DeleteProfilePicState> = _deleteProfilePicState

    private val _getPreferredCoachState = MutableStateFlow<GetPreferredCoachState>(
        GetPreferredCoachState.Idle)
    @NativeCoroutinesState
    val getPreferredCoachState: StateFlow<GetPreferredCoachState> = _getPreferredCoachState

    private val _balance = MutableStateFlow<Double?>(null)
    @NativeCoroutinesState
    val balance: StateFlow<Double?> = _balance

    private val _ewalletTransactions = MutableStateFlow<EwalletUiState>(EwalletUiState.Loading)
    @NativeCoroutinesState
    val ewalletTransactions: StateFlow<EwalletUiState> = _ewalletTransactions.asStateFlow()

    /**
     * Recibe un User “candidato” (con campos fullName, dateOfBirth = "yyyy-MM-dd",
     * sex, phone, postcode, dni, etc.). Primero lo valida mediante el caso de uso; si hay
     * errores, emite ValidationErrors con el mapa. Si no, emite Loading y llama a updateUser()
     * del caso de uso.
     */
    fun updateUser(candidate: User, profilePicBytes: ByteArray?) {
        val validation = UserValidator.validateProfile(
            fullName = candidate.fullName,
            dateOfBirthText = candidate.dateOfBirth,
            selectedSexBackend = candidate.sex,
            phone = candidate.phone,
            postAddress = candidate.postAddress,
            dni = candidate.dni ?: ""
        )

        if (validation is EditValidationResult.Error) {
            val fieldErrors = validation.fieldErrors.mapKeys { (campo, _) ->
                when (campo) {
                    EditValidationResult.Field.FULL_NAME -> UpdateState.Field.FULL_NAME
                    EditValidationResult.Field.DATE_OF_BIRTH -> UpdateState.Field.DATE_OF_BIRTH
                    EditValidationResult.Field.SEX -> UpdateState.Field.SEX
                    EditValidationResult.Field.PHONE -> UpdateState.Field.PHONE
                    EditValidationResult.Field.POST_ADDRESS -> UpdateState.Field.POST_ADDRESS
                    EditValidationResult.Field.DNI -> UpdateState.Field.DNI
                }
            }
            _updateState.value = UpdateState.ValidationErrors(fieldErrors)
            return
        }

        _updateState.value = UpdateState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = userUseCase.updateUser(candidate, profilePicBytes)

            result.onSuccess { newUser ->
                _updateState.value = UpdateState.Success(newUser)
                _userData.value = newUser
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

    fun fetchUserProfile() {
        val currentUser = _userData.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val result = userUseCase.getUserById(currentUser.id)
            result.onSuccess { updatedUser ->
                _userData.value = updatedUser
                SecureStorage.saveUser(updatedUser)
            }.onFailure {
                // Manejar error si es necesario
                log.debug { "❌ Error al refrescar perfil: ${it.message}" }
            }
        }
    }

    fun deleteUser(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _deleteState.value = DeleteUserState.Loading

            userUseCase.deleteUser(email).fold(
                onSuccess = {
                    SecureStorage.clear()
                    delay(1000)

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

    fun logout(onSuccess: () -> Unit) {
        if (_isLoggingOut.value) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoggingOut.value = true

                SecureStorage.clear()
                log.debug { "DEBUG: Almacenamiento local eliminado" }

                // Esto permite que el CircularProgressIndicator se vea y la UX sea fluida
                delay(800)

                _isLoggingOut.value = false

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                _isLoggingOut.value = false
                log.debug { "DEBUG: Error en logout: ${e.message}" }
            }
        }
    }

    fun getCoaches() {
        _coachesState.value = CoachState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            userUseCase.getCoaches().onSuccess { professionals ->
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

    fun deleteProfilePic(user: User) {
        _deleteProfilePicState.value = DeleteProfilePicState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            userUseCase.deleteProfilePic(
                DeleteProfilePicRequest(
                    email = user.email,
                    profilePictureName = user.profilePictureName
                )
            ).fold(
                onSuccess = {
                    _deleteProfilePicState.value = DeleteProfilePicState.Success

                    _userData.value = _userData.value?.copy(profilePictureName = null)

                    SecureStorage.saveUser(_userData.value!!)
                },
                onFailure = { throwable ->
                    _deleteProfilePicState.value =
                        DeleteProfilePicState.Error(throwable.message ?: "Error desconocido")
                }
            )
        }
    }

    fun clearDeleteProfilePicState() {
        _deleteProfilePicState.value = DeleteProfilePicState.Idle
    }

    fun markFavorite(coachId: Int, serviceName: String?, userId: Int?) {
        _markFavoriteState.value = MarkFavoriteState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            userUseCase.markFavorite(coachId, serviceName, userId).onSuccess { message ->
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Asume que getPreferredCoach devuelve un objeto con coachId
                userUseCase.getPreferredCoach(
                    customerId = userId
                ).onSuccess { preferred ->
                    // Si éxito, emitimos el coachId
                    _getPreferredCoachState.value =
                        GetPreferredCoachState.Success(preferred.coachId)
                }
                .onFailure { throwable ->
                    // Si falla, emitimos el mensaje de error
                    _getPreferredCoachState.value =
                        GetPreferredCoachState.Error(
                            throwable.message ?: "Error al obtener favorito"
                        )
                }
            } catch (e: Throwable) {
                _getPreferredCoachState.value =
                    GetPreferredCoachState.Error(e.message ?: "Error al obtener favorito")
            }
        }
    }

    fun clearGetPreferredCoachState() {
        _getPreferredCoachState.value = GetPreferredCoachState.Idle
    }

    fun loadUserCoupon(userId: Int) = viewModelScope.launch(Dispatchers.IO) {
        userUseCase.getUserCoupons(userId)
            .onSuccess { coupons ->
                _couponUiState.update { it.copy(currentCoupons = coupons) }
            }
            .onFailure { ex ->
                _couponUiState.update { it.copy(error = ex.message) }
            }
    }

    fun onCouponCodeChanged(code: String) {
        _couponUiState.update { it.copy(code = code, error = null) }
    }

    fun addCouponToUser(userId: Int, code: String) = viewModelScope.launch(Dispatchers.IO) {
        _couponUiState.update { it.copy(isLoading = true, error = null) }

        userUseCase.addCouponToUser(userId, code)
            .onSuccess {
                val updatedCoupons = userUseCase.getUserCoupons(userId).getOrElse { emptyList() }
                _couponUiState.update {
                    it.copy(
                        isLoading = false,
                        currentCoupons = updatedCoupons,
                        code = ""
                    )
                }
            }
            .onFailure { ex ->
                _couponUiState.update { it.copy(isLoading = false, error = ex.message) }
            }
    }

    fun uploadDocument(name: String, data: ByteArray) {
        _uploadState.value = UploadState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            userUseCase.uploadDocument(name, data)
                .onSuccess { message ->
                    _uploadState.value = UploadState.Success(message)
                }
                .onFailure { throwable ->
                    _uploadState.value = UploadState.Error(
                        throwable.message.orEmpty().ifEmpty {
                            "Error desconocido al subir documento"
                        }
                    )
                }
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }

    fun fetchUserBookings(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _userBookings.value = FetchUserBookingsState.Loading

            userUseCase.getUserBookings(userId).onSuccess { bookings ->
                    if (bookings.isEmpty()) {
                        _userBookings.value = FetchUserBookingsState.Success(
                            emptyList()
                        )
                    } else {
                        _userBookings.value = FetchUserBookingsState.Success(bookings)
                    }
            }.onFailure { exception ->
                _userBookings.value = FetchUserBookingsState.Error(
                    exception.message ?: "Ocurrió un error inesperado"
                )
            }
        }
    }

    fun cancelUserBooking(bookingId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            userUseCase.cancelUserBooking(bookingId).fold(
                onSuccess = {
                    notificationManager.cancelNotification(bookingId)
                    fetchUserBookings(_userData.value?.id ?: 0)
                },
                onFailure = { throwable ->
                    println("❌ Error al cancelar reserva: ${throwable.message}")
                }
            )
        }
    }

    fun loadBalance(userId: Int) {
        if (userId == -1) {
            _balance.value = 0.0
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            userUseCase.getEwalletBalance(userId).fold(
                onSuccess = { nuevoBalance ->
                    _balance.value = nuevoBalance ?: 0.0
                },
                onFailure = { error ->
                    println("❌ Error al cargar el balance: ${error.message}")
                    // Opcional: podrías emitir un evento de error para mostrar un Toast
                }
            )
        }
    }

    fun loadEwalletTransactions(userId: Int) {
        viewModelScope.launch {
            _ewalletTransactions.value = EwalletUiState.Loading

            userUseCase.getEwalletTransactions(userId)
                .onSuccess { list ->
                    _ewalletTransactions.value = EwalletUiState.Success(list)
                }
                .onFailure { e ->
                    _ewalletTransactions.value = EwalletUiState.Error(e.message ?:
                    "Error desconocido")
                }
        }
    }
}