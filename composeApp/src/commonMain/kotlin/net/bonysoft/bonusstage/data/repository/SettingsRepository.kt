package net.bonysoft.bonusstage.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.bonysoft.bonusstage.db.BonusStageDatabase
import net.bonysoft.bonusstage.ui.effects.ScreenEffect

/** Device preferences, kept across sign-out. */
class SettingsRepository(
    private val database: BonusStageDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val queries get() = database.settingsQueries

    /** Hide locked achievements' titles and descriptions. On by default. */
    val hideSpoilers: Flow<Boolean> = boolean(KEY_HIDE_SPOILERS, default = true)

    suspend fun setHideSpoilers(hide: Boolean) = put(KEY_HIDE_SPOILERS, hide.toString())

    /** LCD, scanline or CRT effect over the whole app. Off by default. */
    val screenEffect: Flow<ScreenEffect> = queries.setting(KEY_SCREEN_EFFECT).asFlow().mapToOneOrNull(dispatcher)
        .map { value -> ScreenEffect.entries.firstOrNull { it.name == value } ?: ScreenEffect.Off }

    suspend fun setScreenEffect(effect: ScreenEffect) = put(KEY_SCREEN_EFFECT, effect.name)

    private fun boolean(key: String, default: Boolean): Flow<Boolean> =
        queries.setting(key).asFlow().mapToOneOrNull(dispatcher).map { it?.toBooleanStrictOrNull() ?: default }

    private suspend fun put(key: String, value: String) = withContext(dispatcher) {
        queries.putSetting(key, value)
    }

    private companion object {
        const val KEY_HIDE_SPOILERS = "hide_spoilers"
        const val KEY_SCREEN_EFFECT = "screen_effect"
    }
}
