package fr.gallonemilien.websocketcam.repository

import fr.gallonemilien.websocketcam.model.base.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByUsername(username: String): User?
}