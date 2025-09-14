package fr.gallonemilien.websocketcam.service

import fr.gallonemilien.websocketcam.model.http.AuthError
import fr.gallonemilien.websocketcam.model.http.AuthResponse
import fr.gallonemilien.websocketcam.model.http.AuthSuccess
import fr.gallonemilien.websocketcam.model.http.LoginRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtService: JwtService,
    private val userService: UserService,
    private val cameraService: CameraService
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun authenticate(req: LoginRequest): ResponseEntity<AuthResponse> {
        return try {
            if (userService.isValidUser(req.username.trim(), req.password)) {
                val tokenInfo = jwtService.generateToken(req.username.trim())
                logger.info("Successful authentication for user: ${req.username}")

                ResponseEntity.ok(
                    AuthSuccess(
                        token = tokenInfo.token,
                        expiresAt = tokenInfo.expiresAt,
                        username = req.username.trim()
                    )
                )
            } else {
                logger.warn("Failed authentication attempt for user: ${req.username}")
                ResponseEntity.status(401)
                    .body(AuthError("Invalid credentials", "INVALID_CREDENTIALS"))
            }
        } catch (e: Exception) {
            logger.error("Authentication error for user: ${req.username}", e)
            ResponseEntity.status(500)
                .body(AuthError("Internal server error", "INTERNAL_ERROR"))
        }
    }

    fun verifyClientToken(jwtToken: String, espId: String): Boolean =
        jwtService.verifyToken(jwtToken).also { isValid ->

            val username = this.extractUsernameFromToken(jwtToken) ?: return false
            val user = this.userService.findByUsername(username)
            user?.cameras?.find { it.id == espId } ?: return false
            
            if (!isValid) {
                logger.debug("Invalid client token verification")
            }
            return isValid
        }

    fun verifyESPToken(espToken: String, espId: String): Boolean =
        this.cameraService.getCameraById(espId, true).let { camera ->
            BCrypt.checkpw(espToken, camera.hashedKey)
        }

    fun extractUsernameFromToken(token: String): String? =
        jwtService.extractUsername(token)
}