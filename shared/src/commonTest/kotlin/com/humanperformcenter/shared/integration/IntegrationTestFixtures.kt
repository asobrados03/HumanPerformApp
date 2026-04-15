package integration

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.local.UserProfileLocalDataSource
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.network.HttpClientProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json

internal fun integrationProvider(
    apiEngine: MockEngine,
    authEngine: MockEngine = apiEngine,
): HttpClientProvider {
    val json = Json { ignoreUnknownKeys = true }
    val apiClient = HttpClient(apiEngine) {
        expectSuccess = true
        install(ContentNegotiation) { json(json) }
    }
    val authClient = HttpClient(authEngine) {
        expectSuccess = true
        install(ContentNegotiation) { json(json) }
    }

    return object : HttpClientProvider {
        override val apiClient: HttpClient = apiClient
        override val authClient: HttpClient = authClient
        override val baseUrl: String = "https://api.test"
        override val logoutEvents: SharedFlow<Unit> = MutableSharedFlow()
    }
}

internal class InMemoryAuthLocalDataSource : AuthLocalDataSource {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var user: User? = null

    private val tokenFlow = MutableStateFlow("")
    private val userFlowState = MutableStateFlow<User?>(null)

    override suspend fun getAccessToken(): String? = accessToken
    override suspend fun getRefreshToken(): String? = refreshToken
    override fun accessTokenFlow(): Flow<String> = tokenFlow
    override fun userFlow(): Flow<User?> = userFlowState

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        tokenFlow.value = accessToken
    }

    override suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
        tokenFlow.value = ""
    }

    override suspend fun saveUser(user: User) {
        this.user = user
        userFlowState.value = user
    }

    override suspend fun clear() {
        clearTokens()
        user = null
        userFlowState.value = null
    }
}

internal class InMemoryUserProfileLocalDataSource : UserProfileLocalDataSource {
    var savedUser: User? = null

    override suspend fun saveUser(user: User) {
        savedUser = user
    }

    override suspend fun getUser(): User? = savedUser

    override suspend fun clearUser() {
        savedUser = null
    }
}
