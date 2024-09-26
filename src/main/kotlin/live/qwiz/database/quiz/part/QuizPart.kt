package live.qwiz.database.quiz.part

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import live.qwiz.database.quiz.part.select.Select
import live.qwiz.database.quiz.part.select.option.Option
import live.qwiz.json
import live.qwiz.jsonTree
import live.qwiz.playmode.socket.gamepacket.s2c.base.GameS2CPacket
import live.qwiz.playmode.socket.gamepacket.s2c.base.S2CActions

@Serializable
data class QuizPart(val inputType: String, val isPoll: Boolean, val data: JsonElement) {
    fun getS2CQuestionPacket(): GameS2CPacket {
        return GameS2CPacket(S2CActions.ASK_QUESTION, PartData(inputType, isPoll, data).jsonTree())
    }

    fun wData(): QuizPartContent {
        return Parts.parseFromType(inputType, data)
    }

    @Serializable
    private data class PartData(val type: String, val isPoll: Boolean, val data: JsonElement)
}