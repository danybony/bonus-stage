package net.bonysoft.bonusstage.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.bonysoft.bonusstage.db.BonusStageDatabase

actual fun createTestDatabase(): BonusStageDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    BonusStageDatabase.Schema.create(driver)
    return BonusStageDatabase(driver)
}
