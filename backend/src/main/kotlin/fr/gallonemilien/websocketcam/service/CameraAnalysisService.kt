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

@Service
class CameraAnalysisService {

    private val logger = LoggerFactory.getLogger(CameraAnalysisService::class.java)

    private var previousGrayFrame: Mat? = null
    private var frameCount = 0

    // Process only every {frameSkip} frames to save CPU
    private val frameSkip = 2

    // Resize frames to speed up processing
    private val resizeFactor = 0.5

    init {
        OpenCV.loadLocally()
        logger.info("OpenCV loaded")
    }

    fun processFrame(bytes: ByteArray) {
        frameCount++
        if (frameCount % frameSkip != 0) return  // Skip frames for efficiency

        val mat = Imgcodecs.imdecode(MatOfByte(*bytes), Imgcodecs.IMREAD_COLOR)
        if (mat.empty()) return

        // Resize the frame to reduce the number of pixels to process
        val resized = Mat()
        Imgproc.resize(mat, resized, Size(mat.width() * resizeFactor, mat.height() * resizeFactor))

        detectMotion(resized)
    }

    /**
     * Detect motion by comparing the current grayscale frame with the previous one.
     * This approach highlights differences between consecutive frames, capturing
     * rapid movements while ignoring static parts of the scene.
     *
     * Advantages:
     * - Sensitive to quick movements
     * - Simple to implement
     *
     * Limitations:
     * - Can generate false positives with sudden lighting changes
     * - Slow persistent movements may be harder to detect
     */
    private fun detectMotion(currentFrame: Mat) {
        // Convert the current frame to grayscale for simpler intensity-based comparison
        val currentGray = Mat()
        Imgproc.cvtColor(currentFrame, currentGray, Imgproc.COLOR_BGR2GRAY)

        // Ignore frames that are too dark to reduce false positives
        val meanBrightness = Core.mean(currentGray).`val`[0]
        if (meanBrightness < 20) {
            previousGrayFrame = currentGray.clone()
            return
        }

        previousGrayFrame?.let { prevGray ->
            // Compute the absolute difference between the current frame and the previous one
            // This highlights regions that have changed (movement)
            val diff = Mat()
            Core.absdiff(currentGray, prevGray, diff)

            // Apply a binary threshold to create a mask of moving regions
            // Pixels with a difference greater than 15 become 255 (white), others 0 (black)
            Imgproc.threshold(diff, diff, 15.0, 255.0, Imgproc.THRESH_BINARY)

            // Morphological operations to clean up noise:
            // - Open: removes small isolated white pixels
            // - Close: fills small holes inside detected regions
            val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(3.0, 3.0))
            Imgproc.morphologyEx(diff, diff, Imgproc.MORPH_OPEN, kernel)
            Imgproc.morphologyEx(diff, diff, Imgproc.MORPH_CLOSE, kernel)

            // Count the number of non-zero pixels in the diff mask
            // This gives a measure of how much of the frame has changed
            val nonZero = Core.countNonZero(diff)
            val totalPixels = diff.rows() * diff.cols()
            val motionPercentage = (nonZero.toDouble() / totalPixels) * 100

            // Trigger a motion alert if more than 1% of the frame changed
            if (motionPercentage > 1.0) {
                logger.warn(
                    "Motion detected: $motionPercentage% change (non-zero pixels: $nonZero, brightness: $meanBrightness)"
                )
                // TODO: send alert via WebSocket or other mechanism
            }
        }

        // Update the previous frame to the current one for the next comparison
        previousGrayFrame = currentGray.clone()
    }
}
