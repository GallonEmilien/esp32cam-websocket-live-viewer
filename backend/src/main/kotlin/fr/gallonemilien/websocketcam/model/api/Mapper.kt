package fr.gallonemilien.websocketcam.model.api

import fr.gallonemilien.websocketcam.model.base.Camera

fun Camera.toApi(): CameraApi {
    return CameraApi(
        id = this.id,
        userIds = this.userIds,
        userAdmin = this.userAdmin,
        name = this.name
    )
}