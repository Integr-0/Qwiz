package live.qwiz.config.parts

import kotlinx.serialization.Serializable

@Serializable
data class SessionConfig(val secretEncryptKey: String, val secretSignKey: String)