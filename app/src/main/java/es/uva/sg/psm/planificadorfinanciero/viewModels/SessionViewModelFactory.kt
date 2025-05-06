package es.uva.sg.psm.planificadorfinanciero.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.uva.sg.psm.planificadorfinanciero.data.SessionRepository

class SessionViewModelFactory(private val repository: SessionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}