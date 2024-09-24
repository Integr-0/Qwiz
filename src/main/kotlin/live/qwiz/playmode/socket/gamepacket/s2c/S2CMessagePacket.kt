package live.qwiz.playmode.socket.gamepacket.s2c

import kotlinx.serialization.Serializable

@Serializable
data class S2CMessagePacket(val message: String)