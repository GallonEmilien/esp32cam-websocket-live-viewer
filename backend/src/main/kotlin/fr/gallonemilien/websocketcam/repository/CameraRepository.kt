package fr.gallonemilien.websocketcam.repository

import fr.gallonemilien.websocketcam.model.base.Camera
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CameraRepository : MongoRepository<Camera, String>