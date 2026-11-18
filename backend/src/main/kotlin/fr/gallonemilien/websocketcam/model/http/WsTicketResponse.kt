package fr.gallonemilien.websocketcam.model.http

data class WsTicketResponse(
    val ticket: String,
    val expiresInSeconds: Long
)
