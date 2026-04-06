package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.local.UserProfileLocalDataSource
import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.usecase.UserProfileUseCase
import com.humanperformcenter.shared.domain.usecase.validation.EditValidationResult
import com.humanperformcenter.shared.domain.usecase.validation.UserValidator
import com.humanperformcenter.shared.presentation.ui.DeleteProfilePicState
import com.humanperformcenter.shared.presentation.ui.UpdateState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserProfileViewModel(
    private val userProfileUseCase: UserProfileUseCase,
    private val localDataSource: UserProfileLocalDataSource,
) : ViewModel() {
    companion object {
        val log = logging()
    }

    private val _updateState = MutableStateFlow<UpdateState>(viewModelScope, UpdateState.Idle)
    @NativeCoroutinesState
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _deleteProfilePicState = MutableStateFlow<DeleteProfilePicState>(viewModelScope, DeleteProfilePicState.Idle)
    @NativeCoroutinesState
    val deleteProfilePicState: StateFlow<DeleteProfilePicState> = _deleteProfilePicState

    fun updateUser(candidate: User, profilePicBytes: ByteArray?, currentUser: MutableStateFlow<User?>) {
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

        viewModelScope.launch {
            val result = userProfileUseCase.updateUser(candidate, profilePicBytes)

            result.onSuccess { newUser ->
                _updateState.value = UpdateState.Success(newUser)
                currentUser.value = newUser
                localDataSource.saveUser(newUser)
            }.onFailure { throwable ->
                _updateState.value = UpdateState.Error(throwable.message ?: "Error desconocido")
            }
        }
    }

    fun clearUpdateState() {
        _updateState.value = UpdateState.Idle
    }

    /**
     * Helpers para iOS/Swift: evitan depender de nombres de clases generadas.
     */
    fun updateStateKind(): String = when (_updateState.value) {
        is UpdateState.Idle -> "idle"
        is UpdateState.Loading -> "loading"
        is UpdateState.Success -> "success"
        is UpdateState.Error -> "error"
        is UpdateState.ValidationErrors -> "validation"
    }

    fun updateStateMessage(): String? = when (val state = _updateState.value) {
        is UpdateState.Error -> state.message
        else -> null
    }

    fun updateValidationFieldErrors(): Map<String, String> = when (val state = _updateState.value) {
        is UpdateState.ValidationErrors -> state.fieldErrors.mapKeys { (field, _) -> field.name }
        else -> emptyMap()
    }

    fun updateValidationMessages(): List<String> = updateValidationFieldErrors().values.toList()

    fun fetchUserProfile(currentUser: MutableStateFlow<User?>) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = userProfileUseCase.getUserById(user.id)
            result.onSuccess { updatedUser ->
                currentUser.value = updatedUser
                localDataSource.saveUser(updatedUser)
            }.onFailure {
                log.debug { "❌ Error al refrescar perfil: ${it.message}" }
            }
        }
    }

    fun deleteProfilePic(user: User, currentUser: MutableStateFlow<User?>) {
        _deleteProfilePicState.value = DeleteProfilePicState.Loading
        viewModelScope.launch {
            userProfileUseCase.deleteProfilePicture(
                DeleteProfilePicRequest(
                    email = user.email,
                    profilePictureName = user.profilePictureName
                )
            ).fold(
                onSuccess = {
                    _deleteProfilePicState.value = DeleteProfilePicState.Success
                    currentUser.value = currentUser.value?.copy(profilePictureName = null)
                    currentUser.value?.let { localDataSource.saveUser(it) }
                },
                onFailure = { throwable ->
                    _deleteProfilePicState.value = DeleteProfilePicState.Error(
                        throwable.message ?: "Error desconocido"
                    )
                }
            )
        }
    }

    fun clearDeleteProfilePicState() {
        _deleteProfilePicState.value = DeleteProfilePicState.Idle
    }

    fun deleteProfilePicStateKind(): String = when (_deleteProfilePicState.value) {
        is DeleteProfilePicState.Idle -> "idle"
        is DeleteProfilePicState.Loading -> "loading"
        is DeleteProfilePicState.Success -> "success"
        is DeleteProfilePicState.Error -> "error"
    }

    fun deleteProfilePicStateMessage(): String? = when (val state = _deleteProfilePicState.value) {
        is DeleteProfilePicState.Error -> state.message
        else -> null
    }
}
