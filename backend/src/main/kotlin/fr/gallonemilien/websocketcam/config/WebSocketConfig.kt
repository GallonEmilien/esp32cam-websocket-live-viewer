package fr.gallonemilien.websocketcam.config

import fr.gallonemilien.websocketcam.handler.CameraHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(private val cameraHandler: CameraHandler) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(cameraHandler, "/ws/camera")
            .setAllowedOrigins("*")
    }
}
