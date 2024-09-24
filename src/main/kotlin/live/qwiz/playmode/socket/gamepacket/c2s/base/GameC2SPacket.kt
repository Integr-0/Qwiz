package live.qwiz.playmode.socket.gamepacket.c2s.base

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
open class GameC2SPacket(val action: String, val content: JsonElement)