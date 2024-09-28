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

package live.qwiz.database

import com.mongodb.client.*
import io.ktor.server.application.*
import live.qwiz.config.ConfigSystem
import live.qwiz.database.quiz.QuizDatabaseService
import live.qwiz.database.user.UserDatabaseService

class MainDatabaseHandler {
    companion object {
        private var quizDatabaseService: QuizDatabaseService? = null
        private var userDatabaseService: UserDatabaseService? = null

        fun getQuizDatabaseService(): QuizDatabaseService {
            return quizDatabaseService!!
        }

        fun setQuizDatabaseService(service: QuizDatabaseService) {
            quizDatabaseService = service
        }

        fun getUserDatabaseService(): UserDatabaseService {
            return userDatabaseService!!
        }

        fun setUserDatabaseService(service: UserDatabaseService) {
            userDatabaseService = service
        }
    }
}

fun Application.configureDatabases() {
    val mongoDatabase = connectToMongoDB()
    MainDatabaseHandler.setQuizDatabaseService(QuizDatabaseService(mongoDatabase))
    MainDatabaseHandler.setUserDatabaseService(UserDatabaseService(mongoDatabase))
}

fun Application.connectToMongoDB(): MongoDatabase {
    val connectionString = ConfigSystem.get().db.connectionString
    val databaseName = ConfigSystem.get().db.databaseName

    val mongoClient = MongoClients.create(connectionString)
    val database = mongoClient.getDatabase(databaseName)

    environment.monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return database
}
