package fr.gallonemilien.websocketcam.handler

import fr.gallonemilien.websocketcam.service.CameraConnectionService
import fr.gallonemilien.websocketcam.utils.UriUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler

@Component
class CameraHandler(private val cameraConnectionService: CameraConnectionService) : BinaryWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(CameraHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        // 4 MB limits to comfortably accommodate high-res frames without dropping
        session.binaryMessageSizeLimit = 4 * 1024 * 1024
        session.textMessageSizeLimit = 64 * 1024

        val queryParams = UriUtils.getQueryParams(session.uri)
        val mode = queryParams["mode"] ?: "CLIENT"
        val espId = queryParams["espId"]

        // Support both modern one-time tickets / dedicated apiKeys and legacy jwt query param
        val credential = queryParams["ticket"] ?: queryParams["apiKey"] ?: queryParams["jwt"]

        if (cameraConnectionService.connect(session, credential, mode, espId)) {
            logger.info("WebSocket handshake accepted for mode={} (session={}, camera={})", mode, session.id, espId)
        } else {
            logger.warn("WebSocket handshake rejected for mode={} (session={}, camera={})", mode, session.id, espId)
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized or invalid credentials"))
        }
    }

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val espId = cameraConnectionService.espSessions.entries.find { it.value == session }?.key
        if (espId != null) {
            cameraConnectionService.forwardBinaryFromESP(espId, message)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        cameraConnectionService.disconnect(session)
        logger.debug("WebSocket session {} closed with status {}", session.id, status)
    }
}
