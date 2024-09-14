package live.qwiz.config.parts

import kotlinx.serialization.Serializable

@Serializable
data class ConfigTree(val db: DatabaseConfig, val session: SessionConfig)