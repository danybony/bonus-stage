package net.bonysoft.bonusstage.data.rom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RomMatchingTest {

    private val megaMan = CatalogGame(1448, "Mega Man", 7, "NES / Famicom", "", 8, 80)
    private val sonic = CatalogGame(1, "Sonic the Hedgehog", 1, "Mega Drive", "", 23, 400)
    private val catalog = mapOf(
        RomHash(7, "aaa") to megaMan,
        RomHash(7, "bbb") to megaMan,
        RomHash(1, "ccc") to sonic,
    )

    private fun file(name: String) = RomFile(name, name, 1024)

    @Test
    fun matchesByConsoleAndHash() {
        val result = matchRoms(
            listOf(
                file("Sonic.md") to listOf(RomHash(1, "ccc")),
                file("Mega Man.nes") to listOf(RomHash(7, "aaa")),
                file("Homebrew.nes") to listOf(RomHash(7, "zzz")),
            ),
            catalog,
        )

        assertEquals(listOf("Mega Man", "Sonic the Hedgehog"), result.matched.map { it.game.title })
        assertEquals(listOf("Homebrew.nes"), result.unknown.map { it.name })
        assertEquals(3, result.scanned)
    }

    @Test
    fun sameHashOnAnotherConsoleDoesNotMatch() {
        val result = matchRoms(listOf(file("x.bin") to listOf(RomHash(1, "aaa"))), catalog)

        assertTrue(result.matched.isEmpty())
    }

    @Test
    fun anyCandidateConsoleCanMatch() {
        val result = matchRoms(listOf(file("x.bin") to listOf(RomHash(3, "nope"), RomHash(1, "ccc"))), catalog)

        assertEquals(sonic, result.matched.single().game)
    }

    @Test
    fun twoRevisionsOfTheSameGameCountOnce() {
        val result = matchRoms(
            listOf(file("Mega Man (U).nes") to listOf(RomHash(7, "aaa")), file("Mega Man (E).nes") to listOf(RomHash(7, "bbb"))),
            catalog,
        )

        assertEquals("Mega Man (U).nes", result.matched.single().file.name)
        assertTrue(result.unknown.isEmpty())
    }

    @Test
    fun onlyRomLikeFilesAreCandidates() {
        assertTrue(file("Game.SFC").isCandidate())
        assertTrue(!file("Game.srm").isCandidate())
        assertTrue(!file("cover.jpg").isCandidate())
    }
}
