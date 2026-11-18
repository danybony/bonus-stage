package net.bonysoft.bonusstage.data.cache

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class RaDatesTest {
    private val expected = Instant.parse("2023-10-27T02:52:34Z")

    @Test
    fun parsesSpaceSeparatedUtc() = assertEquals(expected, RaDates.parse("2023-10-27 02:52:34"))

    @Test
    fun parsesIsoWithOffset() = assertEquals(expected, RaDates.parse("2023-10-27T02:52:34+00:00"))

    @Test
    fun parsesMicroseconds() = assertEquals(expected, RaDates.parse("2023-10-27T02:52:34.000000Z"))

    @Test
    fun parsesDateOnly() = assertEquals(Instant.parse("2021-10-16T00:00:00Z"), RaDates.parse("2021-10-16"))

    @Test
    fun rejectsGarbage() {
        assertNull(RaDates.parse("soon"))
        assertNull(RaDates.parse(""))
        assertNull(RaDates.parse(null))
    }
}
