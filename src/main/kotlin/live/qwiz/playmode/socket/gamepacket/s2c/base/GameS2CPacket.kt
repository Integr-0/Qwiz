package live.qwiz.playmode.socket.gamepacket.s2c.base

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
open class GameS2CPacket(val action: String, val content: JsonElement)