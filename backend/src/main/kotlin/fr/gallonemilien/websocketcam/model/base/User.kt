package fr.gallonemilien.websocketcam.model.base

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,
    val username: String,
    val hashPassword: String,
    val isAdmin: Boolean = false,

    @DBRef
    val cameras: List<Camera> = listOf()
)

fun User.toNonNullableId(): String {
    return this.id ?: throw IllegalStateException("User ID is null")
}