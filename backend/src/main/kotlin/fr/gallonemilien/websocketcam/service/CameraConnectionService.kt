package fr.gallonemilien.websocketcam.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

@Service
class CameraConnectionService(
    private val authService: AuthService,
    private val cameraAnalysisService: CameraAnalysisService
) {

    private val logger = LoggerFactory.getLogger(CameraConnectionService::class.java)

    // Thread-safe maps for concurrent WebSocket sessions
    private val clientSubscriptions = ConcurrentHashMap<WebSocketSession, String>()
    val espSessions = ConcurrentHashMap<String, WebSocketSession>()

    // Dedicated background thread pool for computer vision analysis
    // Ensures real-time video forwarding to clients is never stalled by OpenCV processing
    private val analysisExecutor = Executors.newFixedThreadPool(2)

    /**
     * Forwards camera binary frame immediately to all subscribed clients with zero delay,
     * and delegates OpenCV analysis asynchronously to a background thread pool.
     */
    fun forwardBinaryFromESP(espId: String, message: BinaryMessage) {
        val payload = message.payload
        val bytes = ByteArray(payload.remaining())
        payload.get(bytes)

        // 1. Instantly forward to subscribed clients with independent buffers (multi-client safe)
        clientSubscriptions.forEach { (client, subscribedId) ->
            if (subscribedId == espId && client.isOpen) {
                try {
                    client.sendMessage(BinaryMessage(ByteBuffer.wrap(bytes)))
                } catch (ex: Exception) {
                    logger.debug("Failed sending frame to client {}: {}", client.id, ex.message)
                }
            }
        }

        // 2. Offload heavy CV analysis to background thread pool
        analysisExecutor.submit {
            try {
                cameraAnalysisService.processFrame(espId, bytes)
            } catch (e: Exception) {
                logger.debug("Background frame analysis error: {}", e.message)
            }
        }
    }

    fun disconnect(session: WebSocketSession) {
        espSessions.entries.removeIf { entry ->
            val matches = entry.value == session
            if (matches) {
                cameraAnalysisService.clearCamera(entry.key)
                logger.info("ESP camera disconnected: {}", entry.key)
            }
            matches
        }
        val removedCamera = clientSubscriptions.remove(session)
        if (removedCamera != null) {
            logger.debug("Client viewer {} disconnected from camera {}", session.id, removedCamera)
        }
    }

    fun connect(
        session: WebSocketSession,
        credential: String?,
        mode: String?,
        espId: String?
    ): Boolean {
        if (credential.isNullOrBlank() || mode.isNullOrBlank() || espId.isNullOrBlank()) {
            logger.warn("WebSocket connection rejected: missing parameters (mode: {}, espId: {})", mode, espId)
            return false
        }

        return when (mode.uppercase()) {
            "ESP" -> connectESP(session, credential.trim(), espId.trim())
            else -> connectClient(session, credential.trim(), espId.trim())
        }
    }

    private fun connectESP(session: WebSocketSession, apiKey: String, espId: String): Boolean {
        val isValid = authService.verifyESPToken(apiKey, espId)
        if (isValid) {
            // Replace existing session if any
            espSessions.put(espId, session)?.let { oldSession ->
                if (oldSession.isOpen) {
                    try { oldSession.close() } catch (_: Exception) {}
                }
            }
            logger.info("ESP camera successfully connected: {}", espId)
        } else {
            logger.warn("ESP camera authentication failed for id: {}", espId)
        }
        return isValid
    }

    private fun connectClient(session: WebSocketSession, ticketOrJwt: String, espId: String): Boolean {
        val isValid = authService.verifyClientAccess(ticketOrJwt, espId)
        if (isValid) {
            clientSubscriptions[session] = espId
            logger.info("Client successfully subscribed to camera: {}", espId)
        } else {
            logger.warn("Client subscription rejected for camera: {}", espId)
        }
        return isValid
    }
}
