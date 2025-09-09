package fr.gallonemilien.websocketcam.model

sealed class AuthResponse
data class AuthSuccess(val token: String) : AuthResponse()
data class AuthError(val error: String) : AuthResponse()