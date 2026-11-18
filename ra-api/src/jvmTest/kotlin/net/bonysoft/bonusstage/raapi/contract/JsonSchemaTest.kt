package net.bonysoft.bonusstage.raapi.contract

import net.bonysoft.bonusstage.raapi.Fixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsonSchemaTest {

    private val snapshot = JsonSchema.of(Fixtures.GAME_INFO_AND_USER_PROGRESS)

    @Test
    fun numericKeysShareOnePath() {
        assertTrue("$.Achievements.{id}.BadgeName" in snapshot.paths)
        assertFalse(snapshot.paths.keys.any { ".9." in it })
    }

    @Test
    fun renderRoundTrips() {
        assertEquals(snapshot, JsonSchema.parse(snapshot.render()))
    }

    @Test
    fun newFieldIsOnlyAWarning() {
        val live = JsonSchema.of(Fixtures.GAME_INFO_AND_USER_PROGRESS.replace("\"ID\": 1,", "\"ID\": 1, \"NewField\": 2,"))

        val diff = live.diff(snapshot)

        assertFalse(diff.isBreaking)
        assertEquals(listOf("$.NewField"), diff.added)
    }

    @Test
    fun removedOrRetypedFieldsBreak() {
        val live = JsonSchema.of(
            Fixtures.GAME_INFO_AND_USER_PROGRESS
                .replace("\"ForumTopicID\": 112,", "")
                .replace("\"NumDistinctPlayers\": 27080", "\"NumDistinctPlayers\": \"27080\""),
        )

        val diff = live.diff(snapshot)

        assertEquals(listOf("$.ForumTopicID"), diff.removed)
        assertEquals(listOf("$.NumDistinctPlayers: integer -> string"), diff.retyped)
    }

    @Test
    fun nullIsNotATypeChange() {
        val live = JsonSchema.of(Fixtures.GAME_INFO_AND_USER_PROGRESS.replace("\"UserTotalPlaytime\": 60", "\"UserTotalPlaytime\": null"))

        assertFalse(live.diff(snapshot).isBreaking)
    }
}
