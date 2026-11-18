package fr.gallonemilien.websocketcam.service

import fr.gallonemilien.websocketcam.model.base.User
import fr.gallonemilien.websocketcam.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    init {
        val adminPwd = System.getenv("WS_CAM_PASSWORD")
        val adminUsr = System.getenv("WS_CAM_USER")
        if (!adminUsr.isNullOrBlank() && !adminPwd.isNullOrBlank()) {
            try {
                this.createUser(adminUsr.trim(), adminPwd, true)
                logger.info("Admin user created successfully.")
            } catch (e: Exception) {
                logger.debug("Admin user already exists or could not be created: {}", e.message)
            }
        }
    }

    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    fun isValidUser(username: String, password: String): Boolean {
        val user = userRepository.findByUsername(username) ?: return false
        return BCrypt.checkpw(password, user.hashPassword)
    }

    fun getCurrentUser(): User? {
        val auth = SecurityContextHolder.getContext().authentication ?: return null
        val principal = auth.principal
        return if (principal is User) principal else null
    }

    fun createUser(username: String, plainPassword: String, isAdmin: Boolean = false): User {
        if (userRepository.findByUsername(username) != null) {
            throw IllegalArgumentException("User already exists")
        }

        val hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt())

        val newUser = User(
            username = username,
            hashPassword = hashedPassword,
            cameras = emptyList(),
            isAdmin = isAdmin
        )

        return userRepository.save(newUser)
    }
}
