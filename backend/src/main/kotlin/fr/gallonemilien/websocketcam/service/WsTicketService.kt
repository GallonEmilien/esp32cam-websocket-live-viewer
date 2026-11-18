package fr.gallonemilien.websocketcam.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

data class WsTicket(
    val userId: String,
    val cameraId: String,
    val expiresAt: Long
)

@Service
class WsTicketService {

    private val logger = LoggerFactory.getLogger(WsTicketService::class.java)
    private val tickets = ConcurrentHashMap<String, WsTicket>()
    private val secureRandom = SecureRandom()
    private val ticketValiditySeconds = 15L

    fun generateTicket(userId: String, cameraId: String): String {
        cleanExpiredTickets()

        val randomBytes = ByteArray(32)
        secureRandom.nextBytes(randomBytes)
        val ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)

        val expiresAt = System.currentTimeMillis() + (ticketValiditySeconds * 1000)
        tickets[ticket] = WsTicket(userId = userId, cameraId = cameraId, expiresAt = expiresAt)

        logger.debug("Generated one-time WS ticket for user {} and camera {}", userId, cameraId)
        return ticket
    }

    /**
     * Validates and immediately consumes the ticket (One-Time Token pattern).
     */
    fun validateAndConsumeTicket(ticket: String, cameraId: String): Boolean {
        cleanExpiredTickets()

        val wsTicket = tickets.remove(ticket) ?: run {
            logger.warn("WebSocket ticket not found or already consumed")
            return false
        }

        if (System.currentTimeMillis() > wsTicket.expiresAt) {
            logger.warn("WebSocket ticket has expired for camera {}", cameraId)
            return false
        }

        if (wsTicket.cameraId != cameraId) {
            logger.warn("WebSocket ticket camera mismatch: expected {} but was {}", wsTicket.cameraId, cameraId)
            return false
        }

        logger.debug("Successfully validated and consumed WS ticket for user {} and camera {}", wsTicket.userId, cameraId)
        return true
    }

    fun getTicketValiditySeconds(): Long = ticketValiditySeconds

    private fun cleanExpiredTickets() {
        val now = System.currentTimeMillis()
        tickets.entries.removeIf { it.value.expiresAt < now }
    }
}
