package fr.gallonemilien.websocketcam.model.base

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "cameras")
data class Camera(
    @Id
    val id: String? = null,
    val hashedKey: String,

    val userIds: List<String>,
    val userAdmin: String,
)

fun Camera.isAdmin(userId: String): Boolean {
    return this.userAdmin == userId
}

fun Camera.isMember(userId: String): Boolean {
    return this.userIds.contains(userId)
}
