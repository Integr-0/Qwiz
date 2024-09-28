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

package live.qwiz.database.user

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import live.qwiz.util.HashGenerator
import org.bson.Document

class UserDatabaseService(database: MongoDatabase) {
    private var collection: MongoCollection<Document>

    init {
        database.createCollection("user_list")
        collection = database.getCollection("user_list")
    }

    suspend fun insert(user: ServerUser): String = coroutineScope {
        val doc = user.toDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }

    suspend fun getById(id: String): ServerUser? = coroutineScope {
        collection.find(Filters.eq("id", id)).first()?.let(ServerUser.Companion::fromDocument)
    }

    suspend fun get(username: String): ServerUser? = coroutineScope {
        collection.find(Filters.eq("name", username)).first()?.let(ServerUser.Companion::fromDocument)
    }

    suspend fun update(id: String, user: ServerUser): Document? = coroutineScope {
        collection.findOneAndReplace(Filters.eq("id", id), user.toDocument())
    }

    suspend fun delete(id: String): Document? = coroutineScope {
        collection.findOneAndDelete(Filters.eq("id", id))
    }

    fun getNewId(): String {
        return runBlocking {
            var id = HashGenerator.generateHash()

            while (getById(id) != null) {
                id = HashGenerator.generateHash()
            }

            return@runBlocking id
        }
    }
}