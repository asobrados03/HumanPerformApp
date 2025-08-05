package com.humanperformcenter.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.humanperformcenter.shared.data.model.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.data.model.UserBooking
import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.shared.domain.usecase.UserUseCase
import com.humanperformcenter.shared.domain.usecase.validation.EditValidationResult
import com.humanperformcenter.shared.domain.usecase.validation.UserValidator
import com.humanperformcenter.ui.viewmodel.state.CoachState
import com.humanperformcenter.ui.viewmodel.state.CouponUiState
import com.humanperformcenter.ui.viewmodel.state.DeleteProfilePicState
import com.humanperformcenter.ui.viewmodel.state.DeleteUserState
import com.humanperformcenter.ui.viewmodel.state.GetPreferredCoachState
import com.humanperformcenter.ui.viewmodel.state.MarkFavoriteState
import com.humanperformcenter.ui.viewmodel.state.UpdateState
import com.humanperformcenter.ui.viewmodel.state.UploadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserViewModel(
    private val userUseCase: UserUseCase
) : ViewModel() {

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    private val _userBookings = MutableStateFlow<List<UserBooking>>(emptyList())
    val userBookings: StateFlow<List<UserBooking>> get() = _userBookings

    private val _couponUiState = MutableStateFlow(CouponUiState())
    val couponUiState: StateFlow<CouponUiState> = _couponUiState.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

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
    val deleteState: StateFlow<DeleteUserState> = _deleteState

    private val _coachesState = MutableStateFlow<CoachState>(CoachState.Idle)
    val coachesState: StateFlow<CoachState> = _coachesState

    private val _markFavoriteState = MutableStateFlow<MarkFavoriteState>(MarkFavoriteState.Idle)
    val markFavoriteState: StateFlow<MarkFavoriteState> = _markFavoriteState

    private val _deleteProfilePicState = MutableStateFlow<DeleteProfilePicState>(DeleteProfilePicState.Idle)
    val deleteProfilePicState: StateFlow<DeleteProfilePicState> = _deleteProfilePicState

    private val _getPreferredCoachState = MutableStateFlow<GetPreferredCoachState>(GetPreferredCoachState.Idle)
    val getPreferredCoachState: StateFlow<GetPreferredCoachState> = _getPreferredCoachState

    /**
     * Recibe un User “candidato” (con campos fullName, dateOfBirth = "yyyy-MM-dd",
     * sex, phone, postcode, dni, etc.). Primero lo valida mediante el caso de uso; si hay
     * errores, emite ValidationErrors con el mapa. Si no, emite Loading y llama a updateUser()
     * del caso de uso.
     */
    fun updateUser(candidate: User, profilePicBytes: ByteArray?) {
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
            postAddress = candidate.postAddress,
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
                    EditValidationResult.Field.POST_ADDRESS -> UpdateState.Field.POST_ADDRESS
                    EditValidationResult.Field.DNI -> UpdateState.Field.DNI
                }
            }
            _updateState.value = UpdateState.ValidationErrors(fieldErrors)
            return
        }

        // 3) Si pasó validación, invocamos updateUser() en Loading
        _updateState.value = UpdateState.Loading

        viewModelScope.launch {
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

    fun deleteProfilePic(user: User) {
        _deleteProfilePicState.value = DeleteProfilePicState.Loading
        viewModelScope.launch {
            userUseCase.deleteProfilePic(
                DeleteProfilePicRequest(
                    email = user.email,
                    profilePictureName = user.profilePictureName
                )
            ).fold(
                onSuccess = {
                    _deleteProfilePicState.value = DeleteProfilePicState.Success

                    _userData.value = _userData.value
                        ?.copy(profilePictureName = null)

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
        viewModelScope.launch {
            userUseCase.markFavorite(coachId, serviceName, userId).onSuccess { message ->
                _markFavoriteState.value = MarkFavoriteState.Success(message)
            }.onFailure { throwable ->
                _markFavoriteState.value = MarkFavoriteState.Error(
                    throwable.message.orEmpty().ifEmpty { "Error desconocido al marcar como favorito" }
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
                            throwable.localizedMessage ?: "Error al obtener favorito"
                        )
                }
            } catch (e: Throwable) {
                _getPreferredCoachState.value =
                    GetPreferredCoachState.Error(e.localizedMessage ?: "Error al obtener favorito")
            }
        }
    }

    fun clearGetPreferredCoachState() {
        _getPreferredCoachState.value = GetPreferredCoachState.Idle
    }

    fun loadUserCoupon(userId: Int) = viewModelScope.launch {
        userUseCase.getUserCoupon(userId)
            .onSuccess { coupon ->
                _couponUiState.update { it.copy(currentCoupon = coupon) }
            }
            .onFailure { ex ->
                _couponUiState.update { it.copy(error = ex.message) }
            }
    }

    fun onCouponCodeChanged(code: String) {
        _couponUiState.update { it.copy(code = code, error = null) }
    }

    fun addCouponToUser(userId: Int, code: String) = viewModelScope.launch {
        _couponUiState.update { it.copy(isLoading = true, error = null) }

        userUseCase.addCouponToUser(userId, code)
            .onSuccess {
                // Una vez añadido, recargamos y desempaquetamos el Result<Coupon?>:
                val updatedCoupon = userUseCase
                    .getUserCoupon(userId)
                    .getOrNull()    // extrae el Coupon? o null si hubo error

                _couponUiState.update { st ->
                    st.copy(
                        isLoading     = false,
                        currentCoupon = updatedCoupon,
                        code          = ""
                    )
                }
            }
            .onFailure { ex ->
                _couponUiState.update { it.copy(isLoading = false, error = ex.message) }
            }
    }

    fun uploadDocument(name: String, data: ByteArray) {
        _uploadState.value = UploadState.Loading
        viewModelScope.launch {
            userUseCase.uploadDocument(name, data)
                .onSuccess { message ->
                    _uploadState.value = UploadState.Success(message)
                }
                .onFailure { throwable ->
                    _uploadState.value = UploadState.Error(
                        throwable.message.orEmpty().ifEmpty { "Error desconocido al subir documento" }
                    )
                }
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }

    fun fetchUserBookings(userId: Int) {
        viewModelScope.launch {
            _userBookings.value = userUseCase.getUserBookings(userId)
        }
    }

    fun cancelUserBooking(bookingId: Int, context: Context) {
        viewModelScope.launch {
            userUseCase.cancelUserBooking(bookingId).fold(
                onSuccess = {
                    println("✅ Reserva cancelada")

                    // ⛔ Cancelar notificación programada
                    WorkManager.getInstance(context)
                        .cancelAllWorkByTag("session_$bookingId")

                    fetchUserBookings(_userData.value?.id ?: 0)
                },
                onFailure = { throwable ->
                    println("❌ Error al cancelar reserva: ${throwable.message}")
                }
            )
        }
    }
}
