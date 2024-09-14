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