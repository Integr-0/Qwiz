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