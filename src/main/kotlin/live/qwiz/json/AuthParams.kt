package live.qwiz.json

import kotlinx.serialization.Serializable

@Serializable
data class AuthParams(val username: String, val passwordHash512: String)