package net.bonysoft.bonusstage.data

import app.cash.sqldelight.driver.native.inMemoryDriver
import net.bonysoft.bonusstage.db.BonusStageDatabase

actual fun createTestDatabase(): BonusStageDatabase = BonusStageDatabase(inMemoryDriver(BonusStageDatabase.Schema))
