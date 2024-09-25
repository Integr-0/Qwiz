package live.qwiz.database.quiz.part

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import live.qwiz.jsonTree
import live.qwiz.playmode.socket.gamepacket.s2c.base.GameS2CPacket
import live.qwiz.playmode.socket.gamepacket.s2c.base.S2CActions

@Serializable
data class QuizPart(val type: String, val isPoll: Boolean, val data: JsonElement) {
    fun getS2CQuestionPacket(): GameS2CPacket {
        return GameS2CPacket(S2CActions.ASK_QUESTION, PartData(type, isPoll, wData()).jsonTree())
    }

    fun wData(): QuizPartContent {
        return Parts.parseFromType(type, data)
    }


    private data class PartData(val type: String, val isPoll: Boolean, val data: Any)
}