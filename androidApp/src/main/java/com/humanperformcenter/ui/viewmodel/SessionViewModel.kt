package com.humanperformcenter.ui.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.data.Session
import com.humanperformcenter.data.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SessionViewModel(
    private val repository: SessionRepository,
    private val prefs: DataStore<Preferences>
) : ViewModel() {

    val getAllSessions: Flow<List<Session>> = repository.getAllSessions()

    private val _entrenamientosContratados = MutableStateFlow(0)
    val entrenamientosContratados: StateFlow<Int> = _entrenamientosContratados

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> get() = _accessToken

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> get() = _userId

    fun setUserCredentials(token: String, id: Int) {
        _accessToken.value = token
        _userId.value = id
    }

    fun insertSession(session: Session) {
        viewModelScope.launch {
            repository.insert(session)
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch {
            repository.delete(session)
        }
    }

    fun comprarEntrenamiento(sesionesPorSemana: Int) {
        _entrenamientosContratados.value = sesionesPorSemana
    }

    companion object {
        private val KEY_ACCESS = stringPreferencesKey("access_token_enc")
    }

    val isLoggedInFlow: Flow<Boolean> = prefs.data
        .map { it[KEY_ACCESS].orEmpty().isNotBlank() }
        .distinctUntilChanged()

    // Esto aquí no va no tiene ningún sentido esto es una vista de modelos
    // Las llamadas al cliente Ktor (que es el object ApiClient) se hacen desde los repositorios
    /*suspend fun realizarReserva(
        coachId: Int,
        hour: String,
        serviceId: Int,
        selectedDate: LocalDate,
        coachCenterId: Int
    ) {
        val token = _accessToken.value ?: return
        val customerId = _userId.value ?: return

        val client = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        try {
            val timeslotResponse: HttpResponse = client.get("https://tu-api.com/api/mobile/timeslot-id") {
                parameter("hour", hour)
            }
            val timeslotJson = Json.parseToJsonElement(timeslotResponse.bodyAsText()).jsonObject
            val sessionTimeslotId = timeslotJson["session_timeslot_id"]
                ?.jsonPrimitive?.content?.toIntOrNull()
                ?: throw Exception("No se pudo obtener session_timeslot_id")

            val productResponse: HttpResponse = client.get("https://tu-api.com/api/mobile/user-product") {
                header("Authorization", "Bearer $token")
            }
            val productJson = Json.parseToJsonElement(productResponse.bodyAsText()).jsonObject
            val productId = productJson["product_id"]
                ?.jsonPrimitive?.content?.toIntOrNull()
                ?: throw Exception("No se pudo obtener product_id")

            val startDateTime = "${selectedDate}T${hour}:00"

            val reservaResponse = client.post("https://tu-api.com/api/mobile/reserve") {
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "customer_id" to customerId,
                        "coach_id" to coachId,
                        "session_timeslot_id" to sessionTimeslotId,
                        "service_id" to serviceId,
                        "product_id" to productId,
                        "center_id" to coachCenterId,
                        "start_date" to startDateTime,
                        "status" to "active",
                        "payment_status" to "pending",
                        "payment_method" to "card"
                    )
                )
            }

            Log.d("Reserva", "✅ Reserva exitosa: ${reservaResponse.status}")

        } catch (e: Exception) {
            Log.e("Reserva", "❌ Error al reservar: ${e.message}")
        } finally {
            client.close()
        }
    }*/
}
