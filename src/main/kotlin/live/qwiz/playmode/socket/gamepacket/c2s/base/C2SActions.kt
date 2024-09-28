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

package live.qwiz.playmode.socket.gamepacket.c2s.base

class C2SActions {
    companion object {
        const val CLIENT_ANSWER = "client_answer"
        const val HOST_PROGRESS = "host_progress"
        const val HOST_LOGIN = "host_login"
        const val CLIENT_LOGIN = "client_login"
    }
}