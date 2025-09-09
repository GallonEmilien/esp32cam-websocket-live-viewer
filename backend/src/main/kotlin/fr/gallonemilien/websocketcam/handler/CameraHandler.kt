package fr.gallonemilien.websocketcam.handler

import fr.gallonemilien.websocketcam.service.CameraService
import fr.gallonemilien.websocketcam.utils.UriUtils
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler

@Component
class CameraHandler(private val cameraService: CameraService) : BinaryWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        session.binaryMessageSizeLimit = 1024 * 1024
        session.textMessageSizeLimit = 1024 * 1024

        val queryParams = UriUtils.getQueryParams(session.uri)
        val jwtToken = queryParams["jwt"]
        val apiKey = queryParams["apiKey"]

        if (cameraService.connect(session, jwtToken, apiKey)) {
            println("${apiKey} connected: ${session.id}")
        } else {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing or invalid JWT/apiKey"))
            return
        }
    }

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        if (session == cameraService.espSession) {
            cameraService.forwardBinaryFromESP(message)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        cameraService.disconnect(session)
        println("Session ${session.id} closed: ${status.reason}")
    }
}
