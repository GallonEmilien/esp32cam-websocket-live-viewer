package fr.gallonemilien.websocketcam.service

import fr.gallonemilien.websocketcam.model.AuthError
import fr.gallonemilien.websocketcam.model.AuthResponse
import fr.gallonemilien.websocketcam.model.AuthSuccess
import fr.gallonemilien.websocketcam.model.LoginRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtService: JwtService,
    private val userService: UserService
) {

    fun authenticate(req: LoginRequest): ResponseEntity<AuthResponse> {
        return if (userService.isValidUser(req.username, req.password)) {
            val token = jwtService.generateToken(req.username)
            ResponseEntity.ok(AuthSuccess(token))
        } else {
            ResponseEntity.status(401).body(AuthError("Invalid credentials"))
        }
    }

    fun verifyClientToken(jwtToken: String): Boolean = jwtService.verifyToken(jwtToken)
    fun verifyESPToken(espToken: String): Boolean = jwtService.verifyEspToken(espToken)
}
