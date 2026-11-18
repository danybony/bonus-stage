package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A tiny pixel-art icon, drawn from rows of `#` (filled) and `.` (empty). */
@Composable
fun PixelIcon(icon: PixelIcons.Icon, color: Color, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Canvas(modifier = modifier.size(size)) {
        val rows = icon.rows
        val cell = this.size.width / rows.first().length
        rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, c ->
                if (c == '#') drawRect(color, Offset(x * cell, y * cell), Size(cell, cell))
            }
        }
    }
}

/** Original 12x12 icons for the app's navigation and status. */
object PixelIcons {
    class Icon(vararg val rows: String)

    val Profile = Icon(
        "....####....",
        "...######...",
        "...######...",
        "...######...",
        "....####....",
        "............",
        "..########..",
        ".##########.",
        ".##########.",
        ".##########.",
        ".##########.",
        "............",
    )

    /** A game cartridge. */
    val Library = Icon(
        ".##########.",
        ".#........#.",
        ".#.######.#.",
        ".#.######.#.",
        ".#.######.#.",
        ".#........#.",
        ".##########.",
        ".##########.",
        ".#.#.##.#.#.",
        ".#.#.##.#.#.",
        ".##########.",
        "............",
    )

    /** A trophy. */
    val Recent = Icon(
        ".##########.",
        "############",
        "#.########.#",
        "#.########.#",
        ".#.######.#.",
        "..########..",
        "....####....",
        ".....##.....",
        ".....##.....",
        "....####....",
        "..########..",
        "............",
    )

    val Wishlist = Icon(
        "............",
        ".###....###.",
        "#####..#####",
        "############",
        "############",
        "############",
        ".##########.",
        "..########..",
        "...######...",
        "....####....",
        ".....##.....",
        "............",
    )

    val Star = Icon(
        ".....##.....",
        ".....##.....",
        "....####....",
        "....####....",
        "############",
        ".##########.",
        "..########..",
        "...######...",
        "...######...",
        "..###..###..",
        "..##....##..",
        ".#........#.",
    )

    val Back = Icon(
        "............",
        ".....#......",
        "....##......",
        "...###......",
        "..##########",
        ".###########",
        ".###########",
        "..##########",
        "...###......",
        "....##......",
        ".....#......",
        "............",
    )

    val Lock = Icon(
        "....####....",
        "...#....#...",
        "..#......#..",
        "..#......#..",
        ".##########.",
        ".##########.",
        ".#####.####.",
        ".####...###.",
        ".#####.####.",
        ".#####.####.",
        ".##########.",
        "............",
    )

    val Eye = Icon(
        "............",
        "............",
        "....####....",
        "..##....##..",
        ".#...##...#.",
        "#...####...#",
        "#...####...#",
        ".#...##...#.",
        "..##....##..",
        "....####....",
        "............",
        "............",
    )

    /** The eye with a diagonal bar: "hidden". */
    val EyeOff = Icon(
        "#...........",
        ".#..........",
        "..#.####....",
        "...#....##..",
        ".#..#.#...#.",
        "#....#.#...#",
        "#...#.#....#",
        ".#...#.#..#.",
        "..##....#...",
        "....####.#..",
        "..........#.",
        "...........#",
    )
}
