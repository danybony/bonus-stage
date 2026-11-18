package net.bonysoft.bonusstage.ui.library

import net.bonysoft.bonusstage.domain.Award
import net.bonysoft.bonusstage.domain.LibraryGame
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryFilterTest {

    private fun game(id: Long, console: Int, award: Award?) =
        LibraryGame(id, "Game $id", console, "Console $console", "", 10, 5, 5, null, award)

    private val library = listOf(
        game(1, 1, null),
        game(2, 1, Award.BeatenHardcore),
        game(3, 2, Award.BeatenSoftcore),
        game(4, 2, Award.Mastered),
        game(5, 2, Award.Completed),
    )

    @Test
    fun statusFilters() {
        assertEquals(listOf(1L), library.filtered(null, StatusFilter.InProgress).map { it.id })
        assertEquals(listOf(2L, 3L), library.filtered(null, StatusFilter.Beaten).map { it.id })
        assertEquals(listOf(4L, 5L), library.filtered(null, StatusFilter.Mastered).map { it.id })
        assertEquals(5, library.filtered(null, StatusFilter.All).size)
    }

    @Test
    fun consoleAndStatusCombine() {
        assertEquals(listOf(3L), library.filtered(2, StatusFilter.Beaten).map { it.id })
    }

    @Test
    fun consolesAreSortedByGameCount() {
        assertEquals(listOf(ConsoleOption(2, "Console 2", 3), ConsoleOption(1, "Console 1", 2)), library.consoleOptions())
    }
}
