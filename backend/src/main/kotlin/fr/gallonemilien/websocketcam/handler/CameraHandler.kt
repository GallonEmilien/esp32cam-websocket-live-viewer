package fr.gallonemilien.websocketcam.handler

import fr.gallonemilien.websocketcam.service.CameraConnectionService
import fr.gallonemilien.websocketcam.utils.UriUtils
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler

@Component
class CameraHandler(private val cameraService: CameraConnectionService) : BinaryWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        session.binaryMessageSizeLimit = 1024 * 1024
        session.textMessageSizeLimit = 1024 * 1024

        val queryParams = UriUtils.getQueryParams(session.uri)
        val jwtToken = queryParams["jwt"]
        val mode = queryParams["mode"]
        val espId = queryParams["espId"]

        if (cameraService.connect(session, jwtToken, mode, espId)) {
            println("${mode} connected: ${session.id} ${espId}")
        } else {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing or invalid JWT/apiKey/permissions"))
            return
        }
    }

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val espId = cameraService.espSessions.entries.find { it.value == session }?.key
        if (espId != null) {
            cameraService.forwardBinaryFromESP(espId, message)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        cameraService.disconnect(session)
        println("Session ${session.id} closed: ${status.reason}")
    }
}
