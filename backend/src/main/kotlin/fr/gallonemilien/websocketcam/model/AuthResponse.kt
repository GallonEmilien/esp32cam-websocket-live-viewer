package fr.gallonemilien.websocketcam.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

sealed class AuthResponse

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuthSuccess(
    val token: String,
    val tokenType: String = "Bearer",
    val expiresAt: Long,
    val username: String? = null
) : AuthResponse()

data class AuthError(
    val error: String,
    val code: String = "AUTH_ERROR",
    val timestamp: Long = Instant.now().epochSecond
) : AuthResponse()

data class ValidationError(
    val field: String,
    val message: String
)

data class AuthValidationError(
    val error: String = "Validation failed",
    val code: String = "VALIDATION_ERROR",
    val details: List<ValidationError>,
    val timestamp: Long = Instant.now().epochSecond
) : AuthResponse()