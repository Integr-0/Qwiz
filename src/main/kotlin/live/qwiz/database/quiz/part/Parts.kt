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

import kotlinx.serialization.json.JsonElement
import live.qwiz.database.quiz.part.select.Select
import live.qwiz.to

class Parts {
    companion object {
        const val SELECT = "select"

        fun parseFromType(type: String, obj: JsonElement): QuizPartContent {
            return when (type) {
                SELECT -> obj.to<Select>()
                else -> throw IllegalArgumentException("Invalid type")
            }
        }
    }
}