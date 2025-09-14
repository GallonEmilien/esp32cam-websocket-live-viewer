package fr.gallonemilien.websocketcam.service

import org.springframework.stereotype.Service
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession

@Service
class CameraConnectionService(private val authService: AuthService) {

    private val clientSubscriptions = mutableMapOf<WebSocketSession, String>()
    var espSessions = mutableMapOf<String, WebSocketSession>()

    fun forwardBinaryFromESP(espId: String, message: BinaryMessage) {
        val data = message.payload
        val copy = data.duplicate()
        clientSubscriptions.filterValues { it == espId }
            .keys
            .forEach { client ->
                if (client.isOpen) {
                    try {
                        client.sendMessage(BinaryMessage(copy))
                    } catch (ex: Exception) {
                        println("Error sending to client ${client.id}: ${ex.message}")
                    }
                }
            }
    }

    fun disconnect(session: WebSocketSession) {
        espSessions.entries.removeIf { it.value == session }
        clientSubscriptions.remove(session)
    }

    fun connect(session: WebSocketSession, jwtToken: String?, mode: String?, espId: String?): Boolean {
        if (jwtToken.isNullOrEmpty() || mode.isNullOrEmpty() || espId.isNullOrEmpty()) return false
        return when (mode) {
            "ESP" -> connectESP(session, jwtToken, espId)
            else -> connectClient(session, jwtToken, espId)
        }
    }

    private fun connectESP(session: WebSocketSession, token: String, espId: String): Boolean =
        authService.verifyESPToken(token, espId).also { if (it) espSessions[espId] = session }

    private fun connectClient(session: WebSocketSession, token: String, espId: String): Boolean =
        authService.verifyClientToken(token, espId).also { if (it) clientSubscriptions[session] = espId }
}
