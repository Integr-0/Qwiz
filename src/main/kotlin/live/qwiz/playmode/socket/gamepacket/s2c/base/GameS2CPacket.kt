package live.qwiz.playmode.socket.gamepacket.s2c.base

import com.google.gson.JsonElement

open class GameS2CPacket(val action: String, val content: JsonElement)