package com.humanperformcenter.shared.session

import com.humanperformcenter.shared.data.model.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
    private val _currentUser = MutableStateFlow<LoginResponse?>(null)
    val user: StateFlow<LoginResponse?> = _currentUser

    fun storeUser(userResponse: LoginResponse) {
        _currentUser.value = userResponse
    }

    fun clearUser() {
        _currentUser.value = null
    }

    fun getCurrentUser(): LoginResponse? {
        return _currentUser.value
    }
}
