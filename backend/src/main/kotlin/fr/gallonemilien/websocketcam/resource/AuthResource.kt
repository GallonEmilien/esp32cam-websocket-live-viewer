package fr.gallonemilien.websocketcam.resource
import fr.gallonemilien.websocketcam.model.AuthResponse
import fr.gallonemilien.websocketcam.model.LoginRequest
import fr.gallonemilien.websocketcam.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthResource(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        return authService.authenticate(req)
    }
}
