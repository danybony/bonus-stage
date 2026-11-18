package net.bonysoft.bonusstage.raapi

import kotlinx.serialization.json.Json

/** JSON configurations for the RA Web API (see CLAUDE.md, "RetroAchievements Web API"). */
object RaJson {

    /** Used by the app: tolerates new fields and nulls where a default exists. */
    val app: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    /**
     * Used by the contract tests: a missing or retyped field fails decoding.
     * New fields are still accepted here; the schema snapshot reports them.
     */
    val strict: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = false
        explicitNulls = false
    }
}
