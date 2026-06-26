package repositories

import java.sql.Connection

class ApiSyncStateRepository(
    private val databaseConnection: Connection
) {

    fun isCompleted(syncName: String): Boolean {
        val sql = """
            SELECT completed
            FROM api_sync_state
            WHERE sync_name = ?
        """.trimIndent()

        databaseConnection.prepareStatement(sql).use { statement ->
            statement.setString(1, syncName)

            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getBoolean("completed")
                }
            }
        }

        return false
    }

    fun markCompleted(syncName: String) {
        val sql = """
            INSERT INTO api_sync_state (
                sync_name,
                completed,
                completed_at,
                updated_at
            ) VALUES (?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (sync_name)
            DO UPDATE SET
                completed = TRUE,
                completed_at = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP
        """.trimIndent()

        databaseConnection.prepareStatement(sql).use { statement ->
            statement.setString(1, syncName)
            statement.executeUpdate()
        }
    }
}
