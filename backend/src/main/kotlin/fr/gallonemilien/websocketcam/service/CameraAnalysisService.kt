package fr.gallonemilien.websocketcam.service

import nu.pattern.OpenCV
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class CameraAnalysisService {

    private val logger = LoggerFactory.getLogger(CameraAnalysisService::class.java)

    // Store previous grayscale frame per camera ID to prevent cross-camera interference
    private val previousGrayFrames = ConcurrentHashMap<String, Mat>()
    private val frameCounters = ConcurrentHashMap<String, AtomicInteger>()

    // Process only every {frameSkip} frames to save CPU
    private val frameSkip = 3

    // Resize frames to speed up processing
    private val resizeFactor = 0.5

    init {
        try {
            OpenCV.loadLocally()
            logger.info("OpenCV loaded successfully")
        } catch (e: Throwable) {
            logger.warn("OpenCV could not be loaded: {}", e.message)
        }
    }

    /**
     * Processes a video frame asynchronously without blocking the WebSocket pipeline.
     * All OpenCV native Mat objects are guaranteed to be released in finally blocks to prevent OOM.
     */
    fun processFrame(espId: String, bytes: ByteArray) {
        val counter = frameCounters.computeIfAbsent(espId) { AtomicInteger(0) }
        if (counter.incrementAndGet() % frameSkip != 0) {
            return
        }

        var rawMat: Mat? = null
        var resizedMat: Mat? = null
        try {
            rawMat = Imgcodecs.imdecode(MatOfByte(*bytes), Imgcodecs.IMREAD_COLOR)
            if (rawMat == null || rawMat.empty()) return

            val targetWidth = (rawMat.width() * resizeFactor).coerceAtLeast(1.0)
            val targetHeight = (rawMat.height() * resizeFactor).coerceAtLeast(1.0)
            resizedMat = Mat()
            Imgproc.resize(rawMat, resizedMat, Size(targetWidth, targetHeight))

            detectMotion(espId, resizedMat)
        } catch (e: Exception) {
            logger.debug("Frame processing error for camera {}: {}", espId, e.message)
        } finally {
            rawMat?.release()
            resizedMat?.release()
        }
    }

    private fun detectMotion(espId: String, currentFrame: Mat) {
        var currentGray: Mat? = null
        var diff: Mat? = null
        var kernel: Mat? = null

        try {
            currentGray = Mat()
            Imgproc.cvtColor(currentFrame, currentGray, Imgproc.COLOR_BGR2GRAY)

            val meanBrightness = Core.mean(currentGray).`val`[0]
            if (meanBrightness < 20.0) {
                updatePreviousFrame(espId, currentGray)
                return
            }

            val prevGray = previousGrayFrames[espId]
            if (prevGray != null && !prevGray.empty() && prevGray.size() == currentGray.size()) {
                diff = Mat()
                Core.absdiff(currentGray, prevGray, diff)
                Imgproc.threshold(diff, diff, 15.0, 255.0, Imgproc.THRESH_BINARY)

                kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(3.0, 3.0))
                Imgproc.morphologyEx(diff, diff, Imgproc.MORPH_OPEN, kernel)
                Imgproc.morphologyEx(diff, diff, Imgproc.MORPH_CLOSE, kernel)

                val nonZero = Core.countNonZero(diff)
                val totalPixels = diff.rows() * diff.cols()
                val motionPercentage = if (totalPixels > 0) (nonZero.toDouble() / totalPixels) * 100 else 0.0

                if (motionPercentage > 1.0) {
                    logger.debug(
                        "Motion detected on camera {}: {}% change (non-zero: {}, brightness: {})",
                        espId, motionPercentage, nonZero, meanBrightness
                    )
                }
            }

            updatePreviousFrame(espId, currentGray)
        } finally {
            diff?.release()
            kernel?.release()
        }
    }

    private fun updatePreviousFrame(espId: String, newGray: Mat) {
        val old = previousGrayFrames.put(espId, newGray.clone())
        old?.release()
    }

    fun clearCamera(espId: String) {
        val old = previousGrayFrames.remove(espId)
        old?.release()
        frameCounters.remove(espId)
    }
}
