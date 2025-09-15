package fr.gallonemilien.websocketcam.model.api

data class CameraApi(
    val id: String? = null,
    val userIds: List<String>,
    val userAdmin: String,
    val name: String
)