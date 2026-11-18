package net.bonysoft.bonusstage.data.account

import io.ktor.client.HttpClient
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import net.bonysoft.bonusstage.data.demo.DemoData
import net.bonysoft.bonusstage.data.demo.demoHttpClient
import net.bonysoft.bonusstage.raapi.RaCredentials
import net.bonysoft.bonusstage.raapi.RequestThrottler
import net.bonysoft.bonusstage.raapi.RetroAchievementsClient
import kotlin.time.Duration.Companion.milliseconds

/**
 * Owns the session: signs in (verifying the key with the API), restores the stored account at
 * start-up and signs out, clearing every [UserDataCleaner]. Also hands out the API client for the
 * current account, the real one or the demo one.
 */
class AccountRepository(
    private val store: CredentialStore,
    httpClient: HttpClient,
    scope: CoroutineScope,
    private val cleaners: () -> List<UserDataCleaner> = { emptyList() },
    demoClient: HttpClient = demoHttpClient(),
) {
    private val _session = MutableStateFlow<Session>(Session.Loading)
    val session: StateFlow<Session> = _session.asStateFlow()

    val currentAccount: Account? get() = (_session.value as? Session.SignedIn)?.account

    private val liveClient = RetroAchievementsClient(
        credentials = { currentAccount?.takeUnless { it.isDemo }?.credentials },
        httpClient = httpClient,
    )

    private val demoApiClient = RetroAchievementsClient(
        credentials = { DemoData.account.credentials },
        httpClient = demoClient,
        // A short pause so the demo shows the same loading states as the real API.
        throttler = RequestThrottler(minInterval = 150.milliseconds),
    )

    init {
        scope.launch {
            val stored = try {
                store.load()?.let { json.decodeFromString(Account.serializer(), it) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
            // A sign-in that finished first wins over the stored account.
            _session.compareAndSet(Session.Loading, stored?.let(Session::SignedIn) ?: Session.SignedOut)
        }
    }

    /** The API client for the signed-in account. */
    fun client(): RetroAchievementsClient = if (currentAccount?.isDemo == true) demoApiClient else liveClient

    /**
     * Verifies the credentials with `API_GetUserProfile` and stores the account under the user's
     * canonical name and ULID. Throws [net.bonysoft.bonusstage.raapi.RaApiException] on failure.
     */
    suspend fun signIn(username: String, webApiKey: String) {
        val credentials = RaCredentials(username.trim(), webApiKey.trim())
        val profile = liveClient.verify(credentials)
        persist(Account(username = profile.user, ulid = profile.ulid, webApiKey = credentials.webApiKey))
    }

    suspend fun signInDemo() = persist(DemoData.account)

    suspend fun signOut() {
        store.clear()
        cleaners().forEach { it.clear() }
        _session.value = Session.SignedOut
    }

    private suspend fun persist(account: Account) {
        store.save(json.encodeToString(Account.serializer(), account))
        _session.value = Session.SignedIn(account)
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
