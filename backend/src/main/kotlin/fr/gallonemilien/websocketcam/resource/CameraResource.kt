package fr.gallonemilien.websocketcam.resource

import fr.gallonemilien.websocketcam.model.api.CameraApi
import fr.gallonemilien.websocketcam.model.api.toApi
import fr.gallonemilien.websocketcam.model.http.CreateCameraResponse
import fr.gallonemilien.websocketcam.service.CameraService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/camera")
class CameraResource(private val cameraService: CameraService) {

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
    fun createCamera(): ResponseEntity<CreateCameraResponse> {
        return ResponseEntity.ok(this.cameraService.createCamera())
    }

    @DeleteMapping("/{id}")
    fun deleteCamera(@PathVariable id: String): ResponseEntity<Void> {
        this.cameraService.deleteCamera(id)
        return ResponseEntity.noContent().build()
    }
}