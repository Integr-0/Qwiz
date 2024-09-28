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

package live.qwiz.database.quiz

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import live.qwiz.util.HashGenerator
import org.bson.Document

class QuizDatabaseService(database: MongoDatabase) {
    private var collection: MongoCollection<Document>

    init {
        database.createCollection("quiz_list")
        collection = database.getCollection("quiz_list")
    }

    suspend fun insert(quiz: Quiz): String = coroutineScope {
        val doc = quiz.toDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }

    suspend fun get(id: String): Quiz? = coroutineScope {
        collection.find(Filters.eq("id", id)).first()?.let(Quiz.Companion::fromDocument)
    }

    suspend fun update(id: String, quiz: Quiz): Document? = coroutineScope {
        collection.findOneAndReplace(Filters.eq("id", id), quiz.toDocument())
    }

    suspend fun delete(id: String): Document? = coroutineScope {
        collection.findOneAndDelete(Filters.eq("id", id))
    }

    fun getNewId(): String {
        return runBlocking {
            var id = HashGenerator.generateHash()

            while (get(id) != null) {
                id = HashGenerator.generateHash()
            }

            return@runBlocking id
        }
    }
}