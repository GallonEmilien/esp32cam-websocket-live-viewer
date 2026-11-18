package fr.gallonemilien.websocketcam.resource

import fr.gallonemilien.websocketcam.model.api.CameraApi
import fr.gallonemilien.websocketcam.model.api.toApi
import fr.gallonemilien.websocketcam.model.base.toNonNullableId
import fr.gallonemilien.websocketcam.model.http.CreateCameraResponse
import fr.gallonemilien.websocketcam.model.http.WsTicketResponse
import fr.gallonemilien.websocketcam.service.CameraService
import fr.gallonemilien.websocketcam.service.UserService
import fr.gallonemilien.websocketcam.service.WsTicketService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/camera")
class CameraResource(
    private val cameraService: CameraService,
    private val userService: UserService,
    private val wsTicketService: WsTicketService
) {

    @GetMapping
    fun getCameras(): ResponseEntity<List<CameraApi>> {
        val cameras = cameraService.getCamerasForCurrentUser()
        return ResponseEntity.ok(cameras.map { it.toApi() })
    }

    @GetMapping("/{id}")
    fun getCameraById(@PathVariable id: String): ResponseEntity<CameraApi> {
        val camera = cameraService.getCameraById(id)
        return ResponseEntity.ok(camera.toApi())
    }

    @PostMapping
    fun createCamera(@RequestBody cameraName: String): ResponseEntity<CreateCameraResponse> {
        return ResponseEntity.ok(this.cameraService.createCamera(cameraName))
    }

    @DeleteMapping("/{id}")
    fun deleteCamera(@PathVariable id: String): ResponseEntity<Void> {
        this.cameraService.deleteCamera(id)
        return ResponseEntity.noContent().build()
    }

    /**
     * Generates a short-lived (15s), one-time WebSocket ticket for secure streaming.
     * Replaces passing permanent JWT tokens in WebSocket URL query parameters.
     */
    @PostMapping("/{id}/ws-ticket")
    fun createWebSocketTicket(@PathVariable id: String): ResponseEntity<WsTicketResponse> {
        // Ensures user has access to camera before issuing ticket
        cameraService.getCameraById(id)

        val currentUser = userService.getCurrentUser()
            ?: return ResponseEntity.status(401).build()

        val ticket = wsTicketService.generateTicket(
            userId = currentUser.toNonNullableId(),
            cameraId = id
        )

        return ResponseEntity.ok(
            WsTicketResponse(
                ticket = ticket,
                expiresInSeconds = wsTicketService.getTicketValiditySeconds()
            )
        )
    }
}
