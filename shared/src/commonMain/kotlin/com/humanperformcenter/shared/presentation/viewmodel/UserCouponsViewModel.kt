package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.domain.usecase.UserCouponUseCase
import com.humanperformcenter.shared.presentation.ui.CouponUiState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UserCouponsViewModel(
    private val userCouponUseCase: UserCouponUseCase,
) : ViewModel() {
    private val _couponUiState = MutableStateFlow(CouponUiState())
    @NativeCoroutinesState
    val couponUiState: StateFlow<CouponUiState> = _couponUiState.asStateFlow()

    fun loadUserCoupons(userId: Int) = viewModelScope.launch {
        _couponUiState.update {
            it.copy(isLoading = true, error = null)
        }

        userCouponUseCase.getUserCoupons(userId).onSuccess { coupons ->
            _couponUiState.update {
                it.copy(isLoading = false, currentCoupons = coupons)
            }
        }.onFailure { ex ->
            _couponUiState.update {
                it.copy(isLoading = false, error = ex.message)
            }
        }
    }

    fun onCouponCodeChanged(code: String) {
        _couponUiState.update { it.copy(code = code, error = null) }
    }

    fun addCouponToUser(userId: Int, code: String) = viewModelScope.launch {
        _couponUiState.update { it.copy(isLoading = true, error = null) }

        userCouponUseCase.addCouponToUser(userId, code).onSuccess {
            userCouponUseCase.getUserCoupons(userId).onSuccess { updatedCoupons ->
                _couponUiState.update {
                    it.copy(isLoading = false, currentCoupons = updatedCoupons, code = "")
                }
            }.onFailure { ex ->
                _couponUiState.update {
                    it.copy(isLoading = false, error = ex.message)
                }
            }
        }.onFailure { ex ->
            _couponUiState.update {
                it.copy(isLoading = false, error = ex.message)
            }
        }
    }
}
