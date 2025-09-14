package fr.gallonemilien.websocketcam.filter

import fr.gallonemilien.websocketcam.repository.UserRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.crypto.SecretKey

@Component
class JwtAuthenticationFilter(
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    private val jwtSecret: SecretKey by lazy {
        val secret = System.getenv("WS_CAM_JWT_SECRET")
            ?: throw IllegalStateException("WS_CAM_JWT_SECRET environment variable is required")
        if (secret.length < 32) {
            throw IllegalStateException("JWT secret must be at least 32 characters long")
        }
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (header != null && header.startsWith("Bearer")) {
            val token = header.substring(7)
            try {
                val claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token)
                    .body

                val username = claims.subject

                val user = userRepository.findByUsername(username)

                if (user != null) {
                    val auth = UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        emptyList()
                    )
                    SecurityContextHolder.getContext().authentication = auth
                }
            } catch (e: Exception) {
                println("Invalid JWT: ${e.message}")
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }
}
