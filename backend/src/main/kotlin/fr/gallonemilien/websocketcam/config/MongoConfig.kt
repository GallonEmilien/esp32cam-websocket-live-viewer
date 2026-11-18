package fr.gallonemilien.websocketcam.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import io.github.cdimascio.dotenv.dotenv
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class MongoConfig {

    private val logger = LoggerFactory.getLogger(MongoConfig::class.java)

    private val dotenv = try {
        dotenv {
            directory = "./"
            filename = ".env"
            ignoreIfMissing = true
        }
    } catch (_: Exception) {
        null
    }

    private val user = System.getenv("MONGO_INITDB_ROOT_USERNAME")
        ?: dotenv?.get("MONGO_INITDB_ROOT_USERNAME")
        ?: "admin"

    private val password = System.getenv("MONGO_INITDB_ROOT_PASSWORD")
        ?: dotenv?.get("MONGO_INITDB_ROOT_PASSWORD")
        ?: "password"

    @Bean
    fun mongoClient(): MongoClient {
        val host = System.getenv("MONGO_HOST")
            ?: if (System.getenv("ACTIVE_ENV") == "prod") "mongodb" else "localhost"
        val port = System.getenv("MONGO_PORT") ?: "27017"
        val uri = "mongodb://${user}:${password}@${host}:${port}"
        
        logger.info("Connecting to MongoDB at {}:{} (database: esp32cam)", host, port)
        return MongoClients.create(uri)
    }

    @Bean
    fun mongoTemplate(mongoClient: MongoClient): MongoTemplate {
        return MongoTemplate(mongoClient, "esp32cam")
    }
}
