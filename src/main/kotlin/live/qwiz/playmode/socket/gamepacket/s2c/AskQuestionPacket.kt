package live.qwiz.playmode.socket.gamepacket.s2c

import live.qwiz.playmode.socket.gamepacket.s2c.part.OptionPart

data class AskQuestionPacket(val title: String, val options: List<OptionPart>)