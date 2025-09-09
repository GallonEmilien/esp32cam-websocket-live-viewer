package fr.gallonemilien.websocketcam.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService {

    private val jwtSecret: SecretKey = Keys.hmacShaKeyFor(System.getenv("WS_CAM_JWT_SECRET").toByteArray())
    private val espSecret: SecretKey = Keys.hmacShaKeyFor(System.getenv("WS_CAM_ESP_SECRET").toByteArray())

    fun generateToken(username: String): String =
        Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + 3600000))
            .signWith(jwtSecret)
            .compact()

    fun verifyToken(jwtToken: String): Boolean = try {
        val claims = Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(jwtToken).body
        (claims.expiration?.time ?: 0L) > System.currentTimeMillis()
    } catch (_: Exception) { false }

    fun verifyEspToken(espToken: String): Boolean = try {
        val claims = Jwts.parserBuilder().setSigningKey(espSecret).build().parseClaimsJws(espToken).body
        val timestamp = (claims["ts"] as? Number)?.toLong() ?: 0L
        kotlin.math.abs(System.currentTimeMillis() / 1000 - timestamp) <= 30
    } catch (_: Exception) { false }
}