package net.bonysoft.bonusstage.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.cache.Freshness
import net.bonysoft.bonusstage.data.cache.RefreshPolicy
import net.bonysoft.bonusstage.data.cache.Refresher
import net.bonysoft.bonusstage.db.BonusStageDatabase
import net.bonysoft.bonusstage.db.Followed_user
import net.bonysoft.bonusstage.db.Friend_unlock
import net.bonysoft.bonusstage.domain.FriendUnlock
import net.bonysoft.bonusstage.raapi.RaMedia
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Recent unlocks of the people the user follows. The API has no feed endpoint, so this costs one
 * call per friend: each refresh fetches at most [RefreshPolicy.friendsPerRefresh] friends, those
 * whose cache is oldest, and never a friend fetched less than [RefreshPolicy.friendUnlocks] ago.
 */
class FriendsRepository(
    private val database: BonusStageDatabase,
    private val accounts: AccountRepository,
    private val refresher: Refresher,
    private val policy: RefreshPolicy,
    private val clock: Clock = Clock.System,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val queries get() = database.cacheQueries

    fun observeFeed(limit: Int = 100): Flow<List<FriendUnlock>> =
        queries.friendFeed(accounts.requireUlid(), limit.toLong()).asFlow().mapToList(dispatcher).map { rows ->
            rows.map {
                FriendUnlock(
                    friendUlid = it.friend_ulid,
                    friendName = it.username,
                    friendAvatarUrl = RaMedia.userPic(it.username),
                    achievementId = it.achievement_id,
                    title = it.title,
                    description = it.description,
                    points = it.points.toInt(),
                    badgeUrl = RaMedia.badge(it.badge_name),
                    hardcore = it.hardcore == 1L,
                    unlockedAt = Instant.fromEpochMilliseconds(it.unlocked_at),
                    gameId = it.game_id,
                    gameTitle = it.game_title,
                    gameIconUrl = RaMedia.image(it.game_icon),
                )
            }
        }

    fun observeFollowingCount(): Flow<Int> =
        queries.followedUsers(accounts.requireUlid()).asFlow().mapToList(dispatcher).map { it.size }

    fun hasBeenFetched(): Boolean = refresher.fetchedAt("following:${accounts.requireUlid()}") != null

    /**
     * Refreshes the follow list when stale, then the unlocks of the friends due for an update.
     * Returns how many friends were fetched. [force] refetches the list and ignores the per-friend
     * minimum age, but still fetches at most [RefreshPolicy.friendsPerRefresh] friends.
     */
    suspend fun refresh(force: Boolean = false): Int {
        val owner = accounts.requireUlid()
        val client = accounts.client()
        refresher.refresh("following:$owner", policy.following, force) {
            val following = fetchAllPages { count, offset -> client.getUsersIFollow(count, offset) }
            database.transaction {
                queries.deleteFollowedUsers(owner)
                following.forEach {
                    queries.insertFollowedUser(Followed_user(owner, it.ulid, it.user, it.points, it.isFollowingMe.toLong()))
                }
                queries.deleteUnfollowedUnlocks(owner)
            }
        }
        val due = friendsDue(owner, force)
        due.forEach { friend ->
            refresher.refresh(friendResource(owner, friend.ulid), policy.friendUnlocks, force = true) {
                val unlocks = client.getUserRecentAchievements(friend.ulid, minutes = WINDOW_MINUTES)
                database.transaction {
                    unlocks.forEach { a ->
                        val unlockedAt = a.date.toEpochMillis() ?: return@forEach
                        queries.insertFriendUnlock(
                            Friend_unlock(
                                owner, friend.ulid, a.achievementId, unlockedAt, a.hardcoreMode.toLong(), a.title,
                                a.description, a.badgeName, a.points.toLong(), a.gameId, a.gameTitle, a.gameIcon,
                            ),
                        )
                    }
                }
            }
        }
        return due.size
    }

    /** Friends whose unlocks are stale, oldest (or never fetched) first, capped per refresh. */
    internal fun friendsDue(owner: String, force: Boolean): List<Followed_user> {
        val now = clock.now()
        return queries.followedUsers(owner).executeAsList()
            .map { it to refresher.fetchedAt(friendResource(owner, it.ulid)) }
            .filter { (_, fetchedAt) -> force || Freshness.isStale(fetchedAt, policy.friendUnlocks, now) }
            .sortedBy { (_, fetchedAt) -> fetchedAt?.toEpochMilliseconds() ?: Long.MIN_VALUE }
            .take(policy.friendsPerRefresh)
            .map { it.first }
    }

    private fun friendResource(owner: String, friend: String) = "friend:$owner:$friend"

    private companion object {
        /** Three days of unlocks per friend. */
        const val WINDOW_MINUTES = 3 * 24 * 60
    }
}
