package live.qwiz.playmode.socket.gamepacket.c2s

import kotlinx.serialization.Serializable

@Serializable
data class AnswerPacket(val num: Int)