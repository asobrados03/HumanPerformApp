package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.domain.usecase.PaymentUseCase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val useCase: PaymentUseCase
) {

    private val _paymentUrl = MutableStateFlow<String?>(null)
    val paymentUrl: StateFlow<String?> = _paymentUrl.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun generarUrlDePago(request: PaymentRequest) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val url = useCase(request)
                _paymentUrl.value = url
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al generar el pago"
            }
        }
    }

    fun limpiarEstado() {
        _paymentUrl.value = null
        _error.value = null
    }
}
