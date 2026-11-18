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
    private val cameraService: CameraService,
    private val wsTicketService: WsTicketService
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun authenticate(req: LoginRequest): ResponseEntity<AuthResponse> {
        return try {
            val username = req.username.trim()
            if (userService.isValidUser(username, req.password)) {
                val tokenInfo = jwtService.generateToken(username)
                logger.info("Successful authentication for user: {}", username)

                ResponseEntity.ok(
                    AuthSuccess(
                        token = tokenInfo.token,
                        expiresAt = tokenInfo.expiresAt,
                        username = username
                    )
                )
            } else {
                logger.warn("Failed authentication attempt for user: {}", username)
                ResponseEntity.status(401)
                    .body(AuthError("Invalid credentials", "INVALID_CREDENTIALS"))
            }
        } catch (e: Exception) {
            logger.error("Authentication error for user: {}", req.username, e)
            ResponseEntity.status(500)
                .body(AuthError("Internal server error", "INTERNAL_ERROR"))
        }
    }

    /**
     * Verifies WebSocket client access using either a one-time ticket (recommended)
     * or a valid JWT bearer token (legacy fallback).
     */
    fun verifyClientAccess(ticketOrJwt: String, espId: String): Boolean {
        // 1. Try one-time WS Ticket first (preferred and most secure)
        if (wsTicketService.validateAndConsumeTicket(ticketOrJwt, espId)) {
            return true
        }

        // 2. Fallback to JWT token validation
        return verifyClientToken(ticketOrJwt, espId)
    }

    fun verifyClientToken(jwtToken: String, espId: String): Boolean {
        if (!jwtService.verifyToken(jwtToken)) {
            logger.debug("Invalid client JWT token")
            return false
        }

        val username = jwtService.extractUsername(jwtToken) ?: return false
        val user = userService.findByUsername(username) ?: return false

        // Admin has access to all cameras
        if (user.isAdmin) {
            return true
        }

        // Otherwise verify membership
        val hasAccess = user.cameras.any { it.id == espId }
        if (!hasAccess) {
            logger.warn("User {} is not authorized to view camera {}", username, espId)
        }
        return hasAccess
    }

    /**
     * Verifies the camera API secret key for the given camera ID.
     * Safely handles non-existent cameras without throwing exceptions.
     */
    fun verifyESPToken(espToken: String, espId: String): Boolean {
        return try {
            val camera = cameraService.getCameraById(espId, byPassCheck = true)
            val isValid = BCrypt.checkpw(espToken, camera.hashedKey)
            if (!isValid) {
                logger.warn("Invalid secret key for camera {}", espId)
            }
            isValid
        } catch (e: Exception) {
            logger.warn("Camera verification failed for ID {}: {}", espId, e.message)
            false
        }
    }

    fun extractUsernameFromToken(token: String): String? =
        jwtService.extractUsername(token)
}
