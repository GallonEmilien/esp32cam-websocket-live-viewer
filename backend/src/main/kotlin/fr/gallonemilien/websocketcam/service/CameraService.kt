package fr.gallonemilien.websocketcam.service

import org.springframework.stereotype.Service
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession

@Service
class CameraService(private val authService: AuthService) {

    private val clients = mutableSetOf<WebSocketSession>()
    var espSession: WebSocketSession? = null

    fun forwardBinaryFromESP(message: BinaryMessage) {
        val data = message.payload
        val copy = data.duplicate()
        clients.forEach { client ->
            if (client.isOpen) {
                try {
                    client.sendMessage(BinaryMessage(copy))
                } catch (ex: Exception) {
                    println("Error when sending to client ${client.id}: ${ex.message}")
                }
            }
        }
    }

    fun disconnect(session: WebSocketSession) {
        if (session == espSession) espSession = null else clients.remove(session)
    }

    fun connect(session: WebSocketSession, jwtToken: String?, apiKey: String?): Boolean {
        if (jwtToken.isNullOrEmpty() || apiKey.isNullOrEmpty()) return false
        return when (apiKey) {
            "ESP" -> connectESP(session, jwtToken)
            else -> connectClient(session, jwtToken)
        }
    }

    private fun connectESP(session: WebSocketSession, token: String): Boolean =
        authService.verifyESPToken(token).also { if (it) espSession = session }

    private fun connectClient(session: WebSocketSession, token: String): Boolean =
        authService.verifyClientToken(token).also { if (it) clients.add(session) }
}
