package live.qwiz.config.parts

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(val connectionString: String, val databaseName: String)