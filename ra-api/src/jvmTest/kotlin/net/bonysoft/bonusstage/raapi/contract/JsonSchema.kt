package net.bonysoft.bonusstage.raapi.contract

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.doubleOrNull

/**
 * The shape of a JSON response: every path with the value types seen there.
 * Array items share the path `[]`; numeric object keys (IDs used as keys) share the path `{id}`.
 */
data class JsonSchema(val paths: Map<String, Set<String>>) {

    fun render(): String = paths.toSortedMap().entries.joinToString("\n", postfix = "\n") { (path, types) ->
        "$path: ${types.sorted().joinToString("|")}"
    }

    /** Compares a live response ([this]) against the committed [snapshot]. */
    fun diff(snapshot: JsonSchema): SchemaDiff {
        val removed = snapshot.paths.keys - paths.keys
        val added = paths.keys - snapshot.paths.keys
        val retyped = paths.keys.intersect(snapshot.paths.keys).mapNotNull { path ->
            val before = snapshot.paths.getValue(path) - NULL
            val now = paths.getValue(path) - NULL
            // A null on either side is not a change: many fields are null for some records only.
            if (before.isEmpty() || now.isEmpty() || now.all { it in before }) null
            else "$path: ${before.sorted().joinToString("|")} -> ${now.sorted().joinToString("|")}"
        }
        return SchemaDiff(removed.sorted(), retyped.sorted(), added.sorted())
    }

    companion object {
        private const val NULL = "null"

        fun of(json: String): JsonSchema {
            val paths = mutableMapOf<String, MutableSet<String>>()
            collect(Json.parseToJsonElement(json), "$", paths)
            return JsonSchema(paths)
        }

        fun parse(rendered: String): JsonSchema = JsonSchema(
            rendered.lineSequence().filter { it.isNotBlank() }.associate { line ->
                val path = line.substringBeforeLast(": ")
                path to line.substringAfterLast(": ").split("|").toSet()
            },
        )

        private fun collect(element: JsonElement, path: String, into: MutableMap<String, MutableSet<String>>) {
            into.getOrPut(path) { mutableSetOf() } += typeOf(element)
            when (element) {
                is JsonObject -> element.forEach { (key, value) ->
                    val segment = if (key.all { it.isDigit() }) "{id}" else key
                    collect(value, "$path.$segment", into)
                }
                is JsonArray -> element.forEach { collect(it, "$path[]", into) }
                else -> Unit
            }
        }

        private fun typeOf(element: JsonElement): String = when (element) {
            is JsonObject -> "object"
            is JsonArray -> "array"
            JsonNull -> NULL
            is JsonPrimitive -> when {
                element.isString -> "string"
                element.booleanOrNull != null -> "boolean"
                element.longOrNull != null -> "integer"
                element.doubleOrNull != null -> "number"
                else -> "unknown"
            }
        }
    }
}

data class SchemaDiff(val removed: List<String>, val retyped: List<String>, val added: List<String>) {
    val isBreaking get() = removed.isNotEmpty() || retyped.isNotEmpty()

    fun describe(): String = buildString {
        removed.forEach { appendLine("- removed $it") }
        retyped.forEach { appendLine("! retyped $it") }
        added.forEach { appendLine("+ added $it") }
    }
}
