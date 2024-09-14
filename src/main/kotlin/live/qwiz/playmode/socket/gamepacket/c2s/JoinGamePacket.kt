package live.qwiz.playmode.socket.gamepacket.c2s

import kotlinx.serialization.Serializable

@Serializable
data class JoinGamePacket(val username: String)