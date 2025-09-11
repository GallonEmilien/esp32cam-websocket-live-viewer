package fr.gallonemilien.websocketcam.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import io.github.cdimascio.dotenv.dotenv
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class MongoConfig {

    private val dotenv = dotenv {
        directory = "/"
        filename = ".env"
        ignoreIfMissing = false
    }

    private val user = dotenv["MONGO_INITDB_ROOT_USERNAME"]
    private val password = dotenv["MONGO_INITDB_ROOT_PASSWORD"]

    @Bean
    fun mongoClient(): MongoClient {
        val host = if (System.getenv("ACTIVE_ENV") == "prod") "mongodb" else "localhost"
        val uri = "mongodb://${user}:${password}@${host}:27017"
        return MongoClients.create(uri)
    }


    @Bean
    fun mongoTemplate(mongoClient: MongoClient): MongoTemplate {
        return MongoTemplate(mongoClient, "esp32cam")
    }
}
