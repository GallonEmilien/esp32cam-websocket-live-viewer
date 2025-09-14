package fr.gallonemilien.websocketcam.service

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

data class TokenInfo(val token: String, val expiresAt: Long)

@Service
class JwtService {

    private val logger = LoggerFactory.getLogger(JwtService::class.java)

    private val jwtExpirationTime = (System.getenv("WS_CAM_JWT_EXPIRATION")?.toLongOrNull() ?: 3600) * 1000

    private val jwtSecret: SecretKey by lazy {
        val secret = System.getenv("WS_CAM_JWT_SECRET")
            ?: throw IllegalStateException("WS_CAM_JWT_SECRET environment variable is required")

        if (secret.length < 32) {
            throw IllegalStateException("JWT secret must be at least 32 characters long")
        }

        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(username: String): TokenInfo {
        val now = Date()
        val expirationDate = Date(now.time + jwtExpirationTime)

        val token = Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expirationDate)
            .setIssuer("websocket-cam-api")
            .claim("username", username)
            .claim("type", "access_token")
            .signWith(jwtSecret, SignatureAlgorithm.HS256)
            .compact()

        return TokenInfo(token, expirationDate.time)
    }

    fun verifyToken(jwtToken: String): Boolean = try {
        val cleanToken = cleanToken(jwtToken)
        val claims = Jwts.parserBuilder()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(cleanToken)
            .body

        val isNotExpired = claims.expiration?.after(Date()) == true
        val isValidType = claims["type"] == "access_token"

        isNotExpired && isValidType
    } catch (e: ExpiredJwtException) {
        logger.debug("JWT token expired: ${e.message}")
        false
    } catch (e: UnsupportedJwtException) {
        logger.warn("Unsupported JWT token: ${e.message}")
        false
    } catch (e: MalformedJwtException) {
        logger.warn("Malformed JWT token: ${e.message}")
        false
    } catch (e: IllegalArgumentException) {
        logger.warn("JWT token compact of handler are invalid: ${e.message}")
        false
    } catch (e: Exception) {
        logger.error("Unexpected error during JWT verification", e)
        false
    }

    fun extractUsername(jwtToken: String): String? = try {
        val cleanToken = cleanToken(jwtToken)
        Jwts.parserBuilder()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(cleanToken)
            .body
            .subject
    } catch (e: Exception) {
        logger.debug("Could not extract username from token: ${e.message}")
        null
    }

    fun getRemainingTimeInSeconds(jwtToken: String): Long? = try {
        val cleanToken = cleanToken(jwtToken)
        val claims = Jwts.parserBuilder()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(cleanToken)
            .body

        val expiration = claims.expiration
        if (expiration != null) {
            maxOf(0L, (expiration.time - System.currentTimeMillis()) / 1000)
        } else null
    } catch (e: Exception) {
        logger.debug("Could not get remaining time from token: ${e.message}")
        null
    }

    private fun cleanToken(token: String): String {
        return if (token.startsWith("Bearer ")) {
            token.substring(7)
        } else {
            token
        }
    }
}