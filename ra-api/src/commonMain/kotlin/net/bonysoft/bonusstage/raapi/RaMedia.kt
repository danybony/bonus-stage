package net.bonysoft.bonusstage.raapi

/** Builds absolute URLs for the images the API returns as paths or badge names. */
object RaMedia {
    const val MEDIA_BASE_URL = "https://media.retroachievements.org"

    /** For paths like `/Images/067895.png` or `/UserPic/MaxMilyin.png`. */
    fun image(path: String): String =
        if (path.startsWith("http")) path else MEDIA_BASE_URL + "/" + path.trimStart('/')

    fun badge(badgeName: String, locked: Boolean = false): String =
        "$MEDIA_BASE_URL/Badge/$badgeName${if (locked) "_lock" else ""}.png"

    fun userPic(username: String): String = "$MEDIA_BASE_URL/UserPic/$username.png"
}
