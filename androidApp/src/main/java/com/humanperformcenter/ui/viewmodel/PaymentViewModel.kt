package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.di.AppModule.googlePayUseCase
import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.data.persistence.GooglePayRepository
import com.humanperformcenter.shared.domain.usecase.GooglePayUseCase
import com.humanperformcenter.shared.domain.usecase.PaymentUseCase
import com.humanperformcenter.ui.viewmodel.state.PaymentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val paymentUseCase: PaymentUseCase,
    private val googlePayUseCase: GooglePayUseCase
): ViewModel() {
    private val _paymentUrl = MutableStateFlow<String?>(null)
    val paymentUrl: StateFlow<String?> = _paymentUrl.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState

    fun generatePaymentURL(request: PaymentRequest) {
        println("🔧 Generando URL de pago...")
        viewModelScope.launch {
            try {
                val url = paymentUseCase.generatePaymentUrl(request)
                _paymentUrl.value = url
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al generar el pago"
            }
        }
    }

    /**
     * Lanza el flujo de Google Pay a partir del JSON de PaymentDataRequest,
     * y luego envía el token al backend dentro del use case.
     */
    fun payWithGooglePay(requestJson: String) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            googlePayUseCase(requestJson).fold(
                onSuccess = { token ->
                    _paymentState.value = PaymentState.Success(token)
                },
                onFailure = { e ->
                    _paymentState.value = PaymentState.Error(e.localizedMessage ?: "Error")
                }
            )
        }
    }


    fun clearState() {
        _paymentUrl.value = null
        _error.value = null
    }
}
