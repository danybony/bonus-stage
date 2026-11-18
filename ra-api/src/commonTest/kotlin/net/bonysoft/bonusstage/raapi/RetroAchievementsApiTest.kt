package net.bonysoft.bonusstage.raapi

import kotlin.test.Test
import kotlin.test.assertTrue

class RetroAchievementsApiTest {
    @Test
    fun baseUrlUsesHttps() {
        assertTrue(RetroAchievementsApi.BASE_URL.startsWith("https://"))
    }
}
