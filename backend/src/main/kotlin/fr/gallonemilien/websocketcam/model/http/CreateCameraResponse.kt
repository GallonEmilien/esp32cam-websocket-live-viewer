package fr.gallonemilien.websocketcam.model.http

data class CreateCameraResponse(
    val cameraToken: String,
    val cameraId: String?
)
