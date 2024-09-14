package live.qwiz.playmode.socket.gamepacket.s2c.part

import kotlinx.serialization.Serializable


@Serializable
data class OptionPart(val title: String, val description: String)