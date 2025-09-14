package fr.gallonemilien.websocketcam.service

import fr.gallonemilien.websocketcam.model.base.*
import fr.gallonemilien.websocketcam.model.http.CreateCameraResponse
import fr.gallonemilien.websocketcam.repository.CameraRepository
import fr.gallonemilien.websocketcam.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64.getUrlEncoder

@Service
class CameraService(
    private val cameraRepository: CameraRepository,
    private val userService: UserService,
    private val userRepository: UserRepository
) {
    private val secureRandom = SecureRandom()

    fun createCamera(): CreateCameraResponse {
        val currentUser = getAuthenticatedUser()
        val cameraToken = generateSecureToken()
        val savedCamera =
            saveCamera(
                BCrypt.hashpw(
                    cameraToken,
                    BCrypt.gensalt()
                ), currentUser
            )
        addCameraToUsers(listOf(currentUser), savedCamera)
        return CreateCameraResponse(cameraToken = cameraToken, cameraId = savedCamera.id)
    }

    fun getCameraById(cameraId: String, byPassCheck: Boolean = false): Camera {
        val camera = cameraRepository.findById(cameraId)
            .orElseThrow { IllegalArgumentException("Camera not found") }

        //Bypass check for ESP authentication
        if (!byPassCheck) {
            val currentUser = getAuthenticatedUser()
            if (!currentUser.isAdmin)
                checkCameraMemberPermission(camera, currentUser.toNonNullableId())
        }

        return camera
    }

    fun getCamerasForCurrentUser(): List<Camera> {
        if (getAuthenticatedUser().isAdmin) {
            return cameraRepository.findAll()
        }
        return getAuthenticatedUser().cameras
    }

    fun deleteCamera(cameraId: String) {
        val camera = cameraRepository.findById(cameraId)
            .orElseThrow { IllegalArgumentException("Camera not found") }

        val currentUser = getAuthenticatedUser()

        if (!currentUser.isAdmin)
            checkCameraAdminPermission(camera, currentUser.toNonNullableId())

        val usersToUpdate = userRepository.findAllById(camera.userIds)
        val updatedUsers = usersToUpdate.map { user ->
            user.copy(cameras = user.cameras.filter { it.id != cameraId })
        }
        userRepository.saveAll(updatedUsers)
        cameraRepository.delete(camera)
    }

    private fun getAuthenticatedUser() =
        userService.getCurrentUser() ?: throw IllegalStateException("No authenticated user found")

    private fun generateSecureToken(): String {
        val randomBytes = ByteArray(32)
        secureRandom.nextBytes(randomBytes)
        return getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }

    private fun saveCamera(hashedToken: String, user: User): Camera {
        val camera = Camera(
            hashedKey = hashedToken,
            userAdmin = user.toNonNullableId(),
            userIds = listOf(user.toNonNullableId())
        )
        return cameraRepository.save(camera)
    }

    private fun addCameraToUsers(users: List<User>, camera: Camera) {
        val updatedUsers = users.map { user -> user.copy(cameras = user.cameras + camera) }
        userRepository.saveAll(updatedUsers)
    }

    private fun checkCameraAdminPermission(camera: Camera, userId: String) {
        if (!camera.isAdmin(userId)) {
            throw IllegalStateException("User does not have admin permission for this camera")
        }
    }

    private fun checkCameraMemberPermission(camera: Camera, userId: String) {
        if (!camera.isMember(userId)) {
            throw IllegalStateException("User does not have member permission for this camera")
        }
    }
}
