package net.bonysoft.bonusstage.raapi.internal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull

/**
 * The API is served by PHP and encodes booleans as `true`, `1` or `"1"` depending on the endpoint.
 */
internal object FlexibleBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("net.bonysoft.bonusstage.raapi.FlexibleBoolean", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): Boolean {
        val element = (decoder as? JsonDecoder)?.decodeJsonElement() ?: return decoder.decodeBoolean()
        val primitive = element as? JsonPrimitive ?: throw SerializationException("Expected a boolean, got $element")
        primitive.booleanOrNull?.let { return it }
        primitive.intOrNull?.let { return it != 0 }
        return when (primitive.content) {
            "1", "true" -> true
            "0", "false", "" -> false
            else -> throw SerializationException("Expected a boolean, got $primitive")
        }
    }

    override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeBoolean(value)
}

/**
 * PHP encodes an empty associative array as `[]`, so a map field arrives as an array when empty.
 */
internal class PhpMapSerializer<V>(valueSerializer: KSerializer<V>) :
    JsonTransformingSerializer<Map<String, V>>(MapSerializer(String.serializer(), valueSerializer)) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element is JsonArray && element.isEmpty()) JsonObject(emptyMap()) else element
}
