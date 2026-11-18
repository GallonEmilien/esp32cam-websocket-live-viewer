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
    private val expectedIssuer = "websocket-cam-api"
    private val expectedType = "access_token"

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
            .setIssuer(expectedIssuer)
            .claim("username", username)
            .claim("type", expectedType)
            .signWith(jwtSecret, SignatureAlgorithm.HS256)
            .compact()

        return TokenInfo(token, expirationDate.time)
    }

    fun validateAndGetClaims(jwtToken: String): Claims? = try {
        val cleanToken = cleanToken(jwtToken)
        val claims = Jwts.parserBuilder()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(cleanToken)
            .body

        val isNotExpired = claims.expiration?.after(Date()) == true
        val isValidType = claims["type"] == expectedType
        val isValidIssuer = claims.issuer == expectedIssuer

        if (isNotExpired && isValidType && isValidIssuer) {
            claims
        } else {
            logger.warn("JWT claims validation failed (expired: {}, type: {}, issuer: {})", !isNotExpired, claims["type"], claims.issuer)
            null
        }
    } catch (e: ExpiredJwtException) {
        logger.debug("JWT token expired: {}", e.message)
        null
    } catch (e: UnsupportedJwtException) {
        logger.warn("Unsupported JWT token: {}", e.message)
        null
    } catch (e: MalformedJwtException) {
        logger.warn("Malformed JWT token: {}", e.message)
        null
    } catch (e: IllegalArgumentException) {
        logger.warn("Invalid JWT argument: {}", e.message)
        null
    } catch (e: Exception) {
        logger.error("Unexpected error during JWT validation", e)
        null
    }

    fun verifyToken(jwtToken: String): Boolean {
        return validateAndGetClaims(jwtToken) != null
    }

    fun extractUsername(jwtToken: String): String? {
        return validateAndGetClaims(jwtToken)?.subject
    }

    fun getRemainingTimeInSeconds(jwtToken: String): Long? = try {
        val claims = validateAndGetClaims(jwtToken)
        val expiration = claims?.expiration
        if (expiration != null) {
            maxOf(0L, (expiration.time - System.currentTimeMillis()) / 1000)
        } else null
    } catch (e: Exception) {
        logger.debug("Could not get remaining time from token: {}", e.message)
        null
    }

    private fun cleanToken(token: String): String {
        return if (token.startsWith("Bearer ")) {
            token.substring(7).trim()
        } else {
            token.trim()
        }
    }
}
