package fr.gallonemilien.websocketcam.resource

import fr.gallonemilien.websocketcam.model.http.AuthResponse
import fr.gallonemilien.websocketcam.model.http.LoginRequest
import fr.gallonemilien.websocketcam.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthResource(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        return authService.authenticate(req)
    }
}
