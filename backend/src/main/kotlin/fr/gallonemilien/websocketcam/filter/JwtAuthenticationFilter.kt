package fr.gallonemilien.websocketcam.filter

import fr.gallonemilien.websocketcam.repository.UserRepository
import fr.gallonemilien.websocketcam.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val userRepository: UserRepository,
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    private val jwtLogger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (!header.isNullOrBlank() && header.startsWith("Bearer ")) {
            val token = header.substring(7).trim()
            try {
                val username = jwtService.extractUsername(token)

                if (!username.isNullOrBlank() && SecurityContextHolder.getContext().authentication == null) {
                    val user = userRepository.findByUsername(username)

                    if (user != null) {
                        val auth = UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            emptyList()
                        )
                        SecurityContextHolder.getContext().authentication = auth
                    }
                }
            } catch (e: Exception) {
                jwtLogger.warn("Authentication failed with JWT: ${e.message}")
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }
}
