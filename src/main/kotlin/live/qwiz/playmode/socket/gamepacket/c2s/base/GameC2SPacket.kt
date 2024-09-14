package live.qwiz.playmode.socket.gamepacket.c2s.base

import com.google.gson.JsonElement

open class GameC2SPacket(val action: String, val content: JsonElement)