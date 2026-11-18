package net.bonysoft.bonusstage.raapi

import kotlinx.serialization.SerializationException
import net.bonysoft.bonusstage.raapi.model.UserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RaJsonTest {

    private val retyped = Fixtures.USER_PROFILE.replace("\"TotalPoints\": 399597", "\"TotalPoints\": \"lots\"")
    private val nulled = Fixtures.USER_PROFILE.replace("\"TotalPoints\": 399597", "\"TotalPoints\": null")
    private val missing = Fixtures.USER_PROFILE.replace("\"ULID\": \"00003EMFWR7XB8SDPEHB3K56ZQ\",", "")
    private val extra = Fixtures.USER_PROFILE.replace("\"ID\": 16446,", "\"ID\": 16446, \"Brand new\": [1, 2],")

    @Test
    fun strictModeFailsOnRetypedField() {
        assertFailsWith<SerializationException> { RaJson.strict.decodeFromString<UserProfile>(retyped) }
    }

    @Test
    fun strictModeFailsOnNullForNonNullField() {
        assertFailsWith<SerializationException> { RaJson.strict.decodeFromString<UserProfile>(nulled) }
    }

    @Test
    fun bothModesFailOnMissingRequiredField() {
        assertFailsWith<SerializationException> { RaJson.strict.decodeFromString<UserProfile>(missing) }
        assertFailsWith<SerializationException> { RaJson.app.decodeFromString<UserProfile>(missing) }
    }

    @Test
    fun bothModesAcceptNewFields() {
        assertEquals(16446, RaJson.strict.decodeFromString<UserProfile>(extra).id)
        assertEquals(16446, RaJson.app.decodeFromString<UserProfile>(extra).id)
    }
}
