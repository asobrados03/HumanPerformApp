package com.humanperformcenter.ui.viewmodel

import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.domain.usecase.PaymentUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel

class PaymentViewModel(
    private val useCase: PaymentUseCase
): ViewModel() {
    private val _paymentUrl = MutableStateFlow<String?>(null)
    val paymentUrl: StateFlow<String?> = _paymentUrl.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun generatePaymentURL(request: PaymentRequest) {
        println("🔧 Generando URL de pago...")
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val url = useCase(request)
                _paymentUrl.value = url
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al generar el pago"
            }
        }
    }

    fun clearState() {
        _paymentUrl.value = null
        _error.value = null
    }
}
