package net.bonysoft.bonusstage.data.demo

import net.bonysoft.bonusstage.data.account.Account
import net.bonysoft.bonusstage.raapi.model.AchievementOfTheWeek
import net.bonysoft.bonusstage.raapi.model.ActiveClaim
import net.bonysoft.bonusstage.raapi.model.Console
import net.bonysoft.bonusstage.raapi.model.GameAchievement
import net.bonysoft.bonusstage.raapi.model.GameInfoAndUserProgress
import net.bonysoft.bonusstage.raapi.model.GameProgress
import net.bonysoft.bonusstage.raapi.model.Page
import net.bonysoft.bonusstage.raapi.model.RecentAchievement
import net.bonysoft.bonusstage.raapi.model.RecentlyPlayedGame
import net.bonysoft.bonusstage.raapi.model.UserAward
import net.bonysoft.bonusstage.raapi.model.UserAwards
import net.bonysoft.bonusstage.raapi.model.UserProfile
import net.bonysoft.bonusstage.raapi.model.UserSummary
import net.bonysoft.bonusstage.raapi.model.WantToPlayGame
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * The bundled demo account: lets people (and store reviewers) try the app without a
 * RetroAchievements key. Games and images are real RA entries; the user, the progress and most
 * achievement titles are made up. Dates are relative to now so the demo always looks recent.
 */
object DemoData {
    const val USERNAME = "BonusDemo"
    const val ULID = "00000000000000000000DEMO00"

    val account = Account(username = USERNAME, ulid = ULID, webApiKey = "demo", isDemo = true)

    private class DemoAchievement(val title: String, val description: String, val points: Int, val type: String? = null)

    private class DemoGame(
        val id: Long,
        val title: String,
        val consoleId: Int,
        val consoleName: String,
        val icon: String,
        val firstBadge: Int,
        val achievements: List<DemoAchievement>,
        /** Indexes of unlocked achievements, with how long ago, and whether in hardcore. */
        val unlocked: Map<Int, Pair<Duration, Boolean>>,
        val award: String? = null,
        val publisher: String? = null,
        val genre: String? = null,
        val released: String? = null,
        val boxArt: String? = null,
    )

    private val games = listOf(
        DemoGame(
            id = 1, title = "Sonic the Hedgehog", consoleId = 1, consoleName = "Mega Drive / Genesis",
            icon = "/Images/067895.png", firstBadge = 250336, publisher = "Sega", genre = "2D Platforming",
            released = "1991-06-23", boxArt = "/Images/051872.png",
            achievements = listOf(
                DemoAchievement("That Was Easy", "Complete the first act in Green Hill Zone", 3, "progression"),
                DemoAchievement("Ring Hoarder", "Hold 200 rings at once", 10),
                DemoAchievement("Marble Run", "Clear Marble Zone without getting hit", 10),
                DemoAchievement("Spring Loaded", "Finish Spring Yard Zone in under two minutes", 10),
                DemoAchievement("Deep Breath", "Clear Labyrinth Zone without using an air bubble", 25, "missable"),
                DemoAchievement("Star Light Express", "Clear Star Light Zone", 5, "progression"),
                DemoAchievement("Scrap Metal", "Clear Scrap Brain Zone", 10, "progression"),
                DemoAchievement("Emerald Hunter", "Collect a Chaos Emerald in a Special Stage", 10),
                DemoAchievement("Full Set", "Collect all six Chaos Emeralds", 25),
                DemoAchievement("Final Zone", "Defeat Dr. Robotnik in the Final Zone", 25, "win_condition"),
            ),
            unlocked = mapOf(
                0 to (40.minutes to true), 1 to (55.minutes to true), 2 to (2.hours to true),
                3 to (1.days to true), 5 to (1.days to true), 7 to (3.days to true), 9 to (12.minutes to true),
            ),
            award = "beaten-hardcore",
        ),
        DemoGame(
            id = 15758, title = "Crazy Kong", consoleId = 27, consoleName = "Arcade",
            icon = "/Images/068578.png", firstBadge = 325551, publisher = "Falcon", genre = "2D Platforming, Arcade",
            released = "1981", boxArt = "/Images/068205.png",
            achievements = listOf(
                DemoAchievement("Prodigy of the Arcade", "Score 200,000 points", 25),
                DemoAchievement("Barrel Hopper", "Jump over 20 barrels in one game", 5),
                DemoAchievement("Ladder Climber", "Reach the top of the first board", 3, "progression"),
                DemoAchievement("Hammer Time", "Smash 10 barrels with the hammer", 5),
                DemoAchievement("Rivet Remover", "Clear the rivet board", 10, "progression"),
                DemoAchievement("Going Up", "Clear the elevator board without losing a life", 10),
                DemoAchievement("Super Smasher III", "Get 6 smashes with a single bottom hammer on any barrel board", 10),
                DemoAchievement("Kong Toppled", "Complete the first loop", 25, "win_condition"),
            ),
            unlocked = (0..7).associateWith { (20.days + (it * 3).hours) to true },
            award = "mastered",
        ),
        DemoGame(
            id = 14715, title = "Pokemon Pinball mini", consoleId = 24, consoleName = "Pokemon Mini",
            icon = "/Images/028399.png", firstBadge = 108301, publisher = "Nintendo", genre = "Pinball",
            released = "2001-11-16",
            achievements = listOf(
                DemoAchievement("Novice", "Clear stage 01 in Quest.", 2, "progression"),
                DemoAchievement("Beginner I", "Clear stages 01 - 05 in Quest.", 5, "progression"),
                DemoAchievement("Bumper Bonanza", "Hit the bumpers 50 times in one ball", 5),
                DemoAchievement("Catch Combo", "Catch three Pokemon in a row", 10),
                DemoAchievement("No Tilt", "Clear a stage without tilting", 5),
                DemoAchievement("High Roller", "Score 1,000,000 points in Score Attack", 25),
            ),
            unlocked = mapOf(0 to (2.days to true), 1 to (2.days to true), 2 to (5.days to false), 4 to (5.days to false)),
            award = "beaten-softcore",
        ),
        DemoGame(
            id = 1448, title = "Mega Man", consoleId = 7, consoleName = "NES / Famicom",
            icon = "/Images/024519.png", firstBadge = 41003, publisher = "Capcom", genre = "Action, Platforming",
            released = "1987-12-17",
            achievements = listOf(
                DemoAchievement("Cut Above", "Defeat Cut Man", 5, "progression"),
                DemoAchievement("Cold Snap", "Defeat Ice Man", 5, "progression"),
                DemoAchievement("Bomb Squad", "Defeat Bomb Man", 5, "progression"),
                DemoAchievement("Fireproof", "Defeat Fire Man", 5, "progression"),
                DemoAchievement("Short Circuit", "Defeat Elec Man", 5, "progression"),
                DemoAchievement("Rock Solid", "Defeat Guts Man", 5, "progression"),
                DemoAchievement("Yellow Fever", "Defeat the Yellow Devil without taking damage", 25),
                DemoAchievement("Wily Defeated", "Beat the game", 25, "win_condition"),
            ),
            unlocked = mapOf(0 to (6.days to true), 1 to (6.days to true), 2 to (8.days to true)),
        ),
        DemoGame(
            id = 3684, title = "Advanced Busterhawk: Gley Lancer", consoleId = 1, consoleName = "Mega Drive / Genesis",
            icon = "/Images/020895.png", firstBadge = 72311, publisher = "Masaya", genre = "Shoot 'em Up",
            released = "1992-07-17",
            achievements = listOf(
                DemoAchievement("Launch", "Clear stage 1", 3, "progression"),
                DemoAchievement("Gunner Mover", "Clear a stage using only the Mover gunner mode", 10),
                DemoAchievement("Halfway Home", "Clear stage 5", 10, "progression"),
                DemoAchievement("No Miss", "Clear stage 1 without losing a life", 10),
                DemoAchievement("Lancer", "Beat the game", 25, "win_condition"),
            ),
            unlocked = mapOf(0 to (15.days to false), 3 to (15.days to false)),
        ),
        DemoGame(
            id = 20246, title = "~Hack~ Knuckles the Echidna in Sonic the Hedgehog", consoleId = 1,
            consoleName = "Mega Drive / Genesis", icon = "/Images/074560.png", firstBadge = 260410,
            achievements = listOf(
                DemoAchievement("Glide Path", "Glide across the gap in Green Hill Zone Act 2", 5),
                DemoAchievement("Wall Climber", "Climb to the top of Marble Zone Act 3", 5),
                DemoAchievement("Red Rival", "Clear Labyrinth Zone", 10, "progression"),
                DemoAchievement("Guardian", "Beat the game as Knuckles", 25, "win_condition"),
            ),
            unlocked = emptyMap(),
        ),
    )

    /** Want to Play list: two games with sets, one without a set and a claim in progress, one without. */
    private val wishlist = listOf(
        WantToPlayGame(1448, "Mega Man", "/Images/024519.png", 7, "NES / Famicom", 80, 8),
        WantToPlayGame(26971, "~Homebrew~ No Place To Hide", "/Images/084916.png", 18, "Nintendo DS", 0, 0),
        WantToPlayGame(2865, "~Hack~ Plumber For All Seasons, A", "/Images/000001.png", 3, "SNES / Super Famicom", 0, 0),
        WantToPlayGame(3684, "Advanced Busterhawk: Gley Lancer", "/Images/020895.png", 1, "Mega Drive / Genesis", 58, 5),
    )

    fun profile() = UserProfile(
        user = USERNAME, ulid = ULID, userPic = "/UserPic/_User.png", memberSince = format(now() - 900.days),
        richPresenceMsg = "Green Hill Zone Act 3, 52 rings", lastGameId = 1,
        totalPoints = points(hardcore = true), totalSoftcorePoints = points(hardcore = false),
        totalTruePoints = points(hardcore = true) * 3, motto = "One more continue.", id = 1,
    )

    fun summary() = UserSummary(
        user = USERNAME, ulid = ULID, userPic = "/UserPic/_User.png", memberSince = format(now() - 900.days),
        richPresenceMsg = "Green Hill Zone Act 3, 52 rings", lastGameId = 1,
        totalPoints = points(hardcore = true), totalSoftcorePoints = points(hardcore = false),
        totalTruePoints = points(hardcore = true) * 3, motto = "One more continue.",
        rank = 48213, totalRanked = 91544, status = "Online",
        recentlyPlayed = games.take(4).mapIndexed { index, game ->
            RecentlyPlayedGame(game.id, game.consoleId, game.consoleName, game.title, game.icon, format(now() - (index * 30).hours), game.achievements.size)
        },
    )

    fun completionProgress() = Page(
        count = games.size, total = games.size,
        results = games.map { game ->
            val lastUnlock = game.unlocked.values.minOfOrNull { it.first }
            GameProgress(
                gameId = game.id, title = game.title, imageIcon = game.icon, consoleId = game.consoleId,
                consoleName = game.consoleName, maxPossible = game.achievements.size,
                numAwarded = game.unlocked.size, numAwardedHardcore = game.unlocked.count { it.value.second },
                mostRecentAwardedDate = lastUnlock?.let { iso(now() - it) },
                highestAwardKind = game.award, highestAwardDate = game.award?.let { lastUnlock?.let { iso(now() - it) } },
            )
        },
    )

    fun gameInfo(gameId: Long): GameInfoAndUserProgress? {
        val game = games.firstOrNull { it.id == gameId } ?: return null
        return GameInfoAndUserProgress(
            id = game.id, title = game.title, consoleId = game.consoleId, consoleName = game.consoleName,
            imageIcon = game.icon, imageBoxArt = game.boxArt, publisher = game.publisher, genre = game.genre,
            released = game.released, numDistinctPlayers = 12_000 + (game.id % 5_000).toInt(),
            numAchievements = game.achievements.size,
            achievements = game.achievements.mapIndexed { index, achievement ->
                val unlock = game.unlocked[index]
                val id = game.id * 100 + index
                id.toString() to GameAchievement(
                    id = id, numAwarded = 9_000 - index * 700, numAwardedHardcore = 5_000 - index * 450,
                    title = achievement.title, description = achievement.description, points = achievement.points,
                    trueRatio = achievement.points * (2 + index % 4), author = "DemoDev",
                    badgeName = (game.firstBadge + index).toString(), displayOrder = index + 1, type = achievement.type,
                    dateEarned = unlock?.let { format(now() - it.first) },
                    dateEarnedHardcore = unlock?.takeIf { it.second }?.let { format(now() - it.first) },
                )
            }.toMap(),
            numAwardedToUser = game.unlocked.size, numAwardedToUserHardcore = game.unlocked.count { it.value.second },
            userTotalPlaytime = game.unlocked.size * 2_400L, highestAwardKind = game.award,
            highestAwardDate = game.award?.let { iso(now() - game.unlocked.values.minOf { unlock -> unlock.first }) },
        )
    }

    fun recentAchievements(minutes: Int): List<RecentAchievement> {
        val cutoff = minutes.minutes
        return games.flatMap { game ->
            game.unlocked.filter { it.value.first <= cutoff }.map { (index, unlock) ->
                val achievement = game.achievements[index]
                RecentAchievement(
                    date = format(now() - unlock.first), hardcoreMode = unlock.second, achievementId = game.id * 100 + index,
                    title = achievement.title, description = achievement.description,
                    badgeName = (game.firstBadge + index).toString(), points = achievement.points,
                    trueRatio = achievement.points * 3, type = achievement.type, gameTitle = game.title,
                    gameIcon = game.icon, gameId = game.id, consoleName = game.consoleName,
                )
            }
        }.sortedByDescending { it.date }
    }

    fun earnedBetween(fromEpochSeconds: Long, toEpochSeconds: Long): List<RecentAchievement> =
        recentAchievements(minutes = 60 * 24 * 365).filter {
            val at = Instant.parse(it.date.replace(' ', 'T') + "Z").epochSeconds
            at in fromEpochSeconds..toEpochSeconds
        }

    fun wantToPlay() = Page(count = wishlist.size, total = wishlist.size, results = wishlist)

    /** Made-up people the demo user follows; each unlocked a few achievements of the demo games. */
    private class DemoFriend(val name: String, val ulid: String, val points: Long, val unlocks: List<Triple<Int, Int, Duration>>)

    private val friends = listOf(
        DemoFriend("PixelPilgrim", "00000000000000000000FRIEND1", 12_480, listOf(Triple(1, 4, 25.minutes), Triple(1, 7, 3.hours), Triple(3, 6, 1.days))),
        DemoFriend("CartridgeKid", "00000000000000000000FRIEND2", 3_215, listOf(Triple(0, 2, 2.hours), Triple(0, 6, 2.hours), Triple(4, 2, 2.days))),
        DemoFriend("SoftcoreSam", "00000000000000000000FRIEND3", 870, listOf(Triple(2, 5, 50.minutes))),
    )

    fun following() = Page(
        count = friends.size,
        total = friends.size,
        results = friends.mapIndexed { index, friend ->
            net.bonysoft.bonusstage.raapi.model.FollowedUser(friend.name, friend.ulid, friend.points, 0, isFollowingMe = index != 2)
        },
    )

    /** Made-up followers: PixelPilgrim and CartridgeKid follow back, RetroRita is not followed. */
    fun followers() = Page(
        count = 3,
        total = 3,
        results = listOf(
            net.bonysoft.bonusstage.raapi.model.Follower(friends[0].name, friends[0].ulid, friends[0].points, 0, amIFollowing = true),
            net.bonysoft.bonusstage.raapi.model.Follower(friends[1].name, friends[1].ulid, friends[1].points, 0, amIFollowing = true),
            net.bonysoft.bonusstage.raapi.model.Follower("RetroRita", "00000000000000000000FRIEND4", 25_310, 0, amIFollowing = false),
        ),
    )

    /** The demo user's profile, a demo player's, or null for an unknown name. */
    fun profileOf(user: String?): UserProfile? {
        if (user == null || user.equals(USERNAME, ignoreCase = true) || user == ULID) return profile()
        val (name, ulid, points) = (friends.map { Triple(it.name, it.ulid, it.points) } + Triple("RetroRita", "00000000000000000000FRIEND4", 25_310L))
            .firstOrNull { it.first.equals(user, ignoreCase = true) || it.second == user } ?: return null
        return UserProfile(
            user = name, ulid = ulid, userPic = "/UserPic/$name.png", memberSince = format(now() - 400.days),
            totalPoints = points, totalSoftcorePoints = 0, totalTruePoints = points * 3, motto = "Demo player.", id = 2,
        )
    }

    /** Recent unlocks of the demo user, or of a demo friend when [user] is one of their ULIDs. */
    fun recentAchievementsOf(user: String?, minutes: Int): List<RecentAchievement> {
        val friend = friends.firstOrNull { it.ulid == user } ?: return recentAchievements(minutes)
        return friend.unlocks.filter { it.third <= minutes.minutes }.map { (gameIndex, achievementIndex, ago) ->
            val game = games[gameIndex]
            val achievement = game.achievements[achievementIndex]
            RecentAchievement(
                date = format(now() - ago), hardcoreMode = gameIndex != 2, achievementId = game.id * 100 + achievementIndex,
                title = achievement.title, description = achievement.description,
                badgeName = (game.firstBadge + achievementIndex).toString(), points = achievement.points,
                trueRatio = achievement.points * 3, type = achievement.type, gameTitle = game.title,
                gameIcon = game.icon, gameId = game.id, consoleName = game.consoleName,
            )
        }
    }

    fun activeClaims() = listOf(
        ActiveClaim(
            id = 11246, user = "DemoDev", gameId = 26971, gameTitle = "~Homebrew~ No Place To Hide",
            gameIcon = "/Images/084916.png", consoleId = 18, consoleName = "Nintendo DS", claimType = 0, setType = 0,
            status = 0, created = format(now() - 12.days), doneTime = format(now() + 78.days), minutesLeft = 78L * 24 * 60,
        ),
    )

    fun awards() = UserAwards(
        totalAwardsCount = 2, hiddenAwardsCount = 0, masteryAwardsCount = 1, completionAwardsCount = 0,
        beatenHardcoreAwardsCount = 1, beatenSoftcoreAwardsCount = 1, eventAwardsCount = 0, siteAwardsCount = 0,
        visibleUserAwards = games.filter { it.award != null }.map { game ->
            UserAward(
                awardedAt = iso(now() - (game.unlocked.values.minOf { it.first })),
                awardType = if (game.award == "mastered") "Mastery/Completion" else "Game Beaten",
                awardData = game.id, awardDataExtra = if (game.award == "beaten-softcore") 0 else 1,
                title = game.title, consoleId = game.consoleId, consoleName = game.consoleName, imageIcon = game.icon,
            )
        },
    )

    /** The demo catalog of a console: the demo and wishlist games, without ROM hashes. */
    fun gameList(consoleId: Int) = games.filter { it.consoleId == consoleId }.map { game ->
        net.bonysoft.bonusstage.raapi.model.GameListEntry(
            id = game.id, title = game.title, consoleId = game.consoleId, consoleName = game.consoleName,
            imageIcon = game.icon, numAchievements = game.achievements.size, points = game.achievements.sumOf { it.points },
        )
    } + wishlist.filter { wanted -> wanted.consoleId == consoleId && games.none { it.id == wanted.gameId } }.map {
        net.bonysoft.bonusstage.raapi.model.GameListEntry(
            id = it.gameId, title = it.title, consoleId = it.consoleId, consoleName = it.consoleName,
            imageIcon = it.imageIcon, numAchievements = it.achievementsPublished, points = it.pointsTotal,
        )
    }

    fun consoles() = (games.map { it.consoleId to it.consoleName } + wishlist.map { it.consoleId to it.consoleName })
        .distinctBy { it.first }
        .map { (id, name) -> Console(id, name, "https://static.retroachievements.org/assets/images/system/unknown.png", active = true, isGameSystem = true) }

    fun achievementOfTheWeek() = AchievementOfTheWeek(
        achievement = AchievementOfTheWeek.Achievement(178634, "Saved Summer", "Defeat the Flower and let Summer Get Busy", 10, 11),
        console = AchievementOfTheWeek.Ref(3, "SNES"), game = AchievementOfTheWeek.Ref(2865, "~Hack~ Plumber For All Seasons, A"),
        startAt = iso(now() - 2.days), totalPlayers = 427, unlocksCount = 280, unlocksHardcoreCount = 268,
    )

    private fun points(hardcore: Boolean) = games.sumOf { game ->
        game.unlocked.filter { it.value.second == hardcore }.keys.sumOf { game.achievements[it].points }
    }.toLong()

    private fun now(): Instant = Clock.System.now()

    /** RA's `2024-01-31 18:04:11` format (UTC). */
    private fun format(instant: Instant): String = iso(instant).replace('T', ' ').removeSuffix("+00:00")

    /** RA's ISO format, `2024-01-31T18:04:11+00:00`. */
    private fun iso(instant: Instant): String =
        Instant.fromEpochSeconds(instant.epochSeconds).toString().removeSuffix("Z") + "+00:00"
}
