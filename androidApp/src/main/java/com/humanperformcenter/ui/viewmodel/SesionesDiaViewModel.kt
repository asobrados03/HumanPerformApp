package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.data.remote.models.SesionesDia
import com.humanperformcenter.data.remote.SesionDiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class SesionesDiaViewModel : ViewModel() {

    private val repository = SesionDiaRepository()

    private val _sessions = MutableStateFlow<List<SesionesDia>>(emptyList())
    val sessions: StateFlow<List<SesionesDia>> get() = _sessions

    fun fetchAvailableSessions(serviceId: Int, date: LocalDate, tipoSesion: String) {
        val weekStart = date.toString()

        println("==== FETCH DE SESIONES DISPONIBLES ====")
        println("Fecha seleccionada: $date")
        println("Inicio de semana: $weekStart")
        println("Tipo de sesión: $tipoSesion")
        println("Service ID enviado: $serviceId")
        println("=======================================")

        viewModelScope.launch {
            try {
                val result = repository.getWeeklySessions(serviceId, weekStart)

                println("---- Resultados recibidos (${result.size}) ----")
                result.forEach {
                    println("→ ${it.date} | ${it.hour} | service_id=${it.service_id} | coach=${it.coach_name} | booked=${it.booked}/${it.capacity}")
                }

                _sessions.value = result.filter {
                    val fecha = it.date.substring(0, 10)
                    val coincideFecha = fecha == date.toString()
                    val coincideTipo = itMatchesTipo(it.service_id, tipoSesion)

                    println("Evaluando: $fecha == ${date} → $coincideFecha, tipoSesion → $coincideTipo")

                    coincideFecha && coincideTipo
                }

                println("→ Sesiones filtradas: ${_sessions.value.size}")
            } catch (e: Exception) {
                println("❌ ERROR AL CONSULTAR SESIONES: ${e.message}")
            }
        }
    }

    private fun itMatchesTipo(serviceId: Int, tipo: String): Boolean {
        return when (tipo.lowercase()) {
            "nutrición" -> serviceId == 1
            "entrenamiento" -> serviceId == 2
            "fisioterapia" -> serviceId == 3
            else -> false
        }
    }
}

