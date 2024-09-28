/*
 * Copyright © 2024 Integr
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package live.qwiz.database.quiz.part

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
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