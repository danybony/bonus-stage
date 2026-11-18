package net.bonysoft.bonusstage.data.cache

import kotlin.time.Instant

/** Parses the API's date formats: `2024-01-31 18:04:11` (UTC), ISO 8601 with offset, and `2024-01-31`. */
object RaDates {
    fun parse(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        val text = value.trim()
        return runCatching {
            when {
                text.length == 10 -> Instant.parse("${text}T00:00:00Z")
                text.length == 19 && text[10] == ' ' -> Instant.parse(text.replace(' ', 'T') + "Z")
                else -> Instant.parse(text)
            }
        }.getOrNull()
    }
}
