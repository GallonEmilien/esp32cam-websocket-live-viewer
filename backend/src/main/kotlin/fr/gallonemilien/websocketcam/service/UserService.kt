package fr.gallonemilien.websocketcam.service

import org.springframework.stereotype.Service

@Service
class UserService {

    private val validUser = System.getenv("WS_CAM_USER")
    private val validPassword = System.getenv("WS_CAM_PASSWORD")

    fun isValidUser(username: String, password: String): Boolean =
        username == validUser && password == validPassword
}
