package net.bonysoft.bonusstage.data

import net.bonysoft.bonusstage.db.BonusStageDatabase

/** A fresh in-memory database with the schema created. */
expect fun createTestDatabase(): BonusStageDatabase
