package net.bonysoft.bonusstage.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/** The four tabs of the bottom bar. Selecting one resets the back stack to it. */
@Serializable
sealed interface TopLevel : NavKey

@Serializable
data object ProfileTab : TopLevel

@Serializable
data object LibraryTab : TopLevel

@Serializable
data object RecentTab : TopLevel

@Serializable
data object WishlistTab : TopLevel

@Serializable
data class GameDetail(val gameId: Long) : NavKey

@Serializable
data object Recap : NavKey

@Serializable
data object RomScan : NavKey

/**
 * iOS has no reflection, so every destination must be registered here to be saved and restored.
 */
val navSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ProfileTab::class, ProfileTab.serializer())
            subclass(LibraryTab::class, LibraryTab.serializer())
            subclass(RecentTab::class, RecentTab.serializer())
            subclass(WishlistTab::class, WishlistTab.serializer())
            subclass(GameDetail::class, GameDetail.serializer())
            subclass(Recap::class, Recap.serializer())
            subclass(RomScan::class, RomScan.serializer())
        }
    }
}
