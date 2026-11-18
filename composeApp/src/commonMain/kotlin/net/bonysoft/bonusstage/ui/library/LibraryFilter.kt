package net.bonysoft.bonusstage.ui.library

import net.bonysoft.bonusstage.domain.Award
import net.bonysoft.bonusstage.domain.LibraryGame

enum class StatusFilter { All, InProgress, Beaten, Mastered }

data class ConsoleOption(val id: Int, val name: String, val count: Int)

fun LibraryGame.matches(status: StatusFilter): Boolean = when (status) {
    StatusFilter.All -> true
    StatusFilter.InProgress -> award == null
    StatusFilter.Beaten -> award == Award.BeatenHardcore || award == Award.BeatenSoftcore
    StatusFilter.Mastered -> award == Award.Mastered || award == Award.Completed
}

fun List<LibraryGame>.filtered(console: Int?, status: StatusFilter): List<LibraryGame> =
    filter { (console == null || it.consoleId == console) && it.matches(status) }

/** Consoles present in the library, most played first. */
fun List<LibraryGame>.consoleOptions(): List<ConsoleOption> =
    groupBy { it.consoleId }
        .map { (id, games) -> ConsoleOption(id, games.first().consoleName, games.size) }
        .sortedWith(compareByDescending<ConsoleOption> { it.count }.thenBy { it.name })
