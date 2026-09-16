package com.screengpt.overlay.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream

class ScreenCaptureManager(private val context: Context, private val onProjectionStopped: () -> Unit = {}) {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var screenWidth = 1080
    private var screenHeight = 2400
    private var screenDensity = 420

    @Volatile
    private var latestAcquiredImage: Image? = null

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            teardownDisplay()
            mediaProjection = null
            onProjectionStopped()
        }
    }

    fun setMediaProjection(projection: MediaProjection) {
        try {
            this.mediaProjection?.unregisterCallback(projectionCallback)
        } catch (e: Exception) {
            // Ignore
        }
        this.mediaProjection?.stop()
        this.mediaProjection = projection
        try {
            projection.registerCallback(projectionCallback, mainHandler)
        } catch (e: Exception) {
            // Ignore
        }
        updateScreenMetrics()
        setupPersistentDisplay()
    }

    fun hasProjection(): Boolean = mediaProjection != null && virtualDisplay != null && imageReader != null

    private fun updateScreenMetrics() {
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)

            if (metrics.widthPixels > 0 && metrics.heightPixels > 0) {
                screenWidth = metrics.widthPixels
                screenHeight = metrics.heightPixels
                screenDensity = metrics.densityDpi
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupPersistentDisplay() {
        val projection = mediaProjection ?: return
        updateScreenMetrics()

        try {
            teardownDisplay()

            val reader = ImageReader.newInstance(
                screenWidth,
                screenHeight,
                PixelFormat.RGBA_8888,
                3
            )
            imageReader = reader

            reader.setOnImageAvailableListener({ r ->
                try {
                    val img = r.acquireLatestImage()
                    if (img != null) {
                        latestAcquiredImage?.close()
                        latestAcquiredImage = img
                    }
                } catch (e: Exception) {
                    // Ignore transient frame acquisition
                }
            }, mainHandler)

            virtualDisplay = projection.createVirtualDisplay(
                "ScreenGPTPersistentCapture",
                screenWidth,
                screenHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.surface,
                null,
                mainHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun captureScreen(): Result<CapturedScreenData> = withContext(Dispatchers.Main.immediate) {
        val projection = mediaProjection ?: return@withContext Result.failure(
            IllegalStateException("Screen capture session expired. Please grant permission.")
        )

        // Ensure display is alive
        if (virtualDisplay == null || imageReader == null) {
            return@withContext Result.failure(IllegalStateException("Screen capture session expired. Please grant permission."))
        }

        try {
            // Wait for a fresh frame (max 1000ms)
            val img = withTimeoutOrNull(1000) {
                while (latestAcquiredImage == null) {
                    delay(20)
                }
                latestAcquiredImage
            }

            val frame = img ?: latestAcquiredImage
            if (frame == null) {
                return@withContext Result.failure(IllegalStateException("Timeout waiting for screen frame."))
            }

            val bitmap = convertImageToBitmap(frame, screenWidth, screenHeight)

            if (bitmap == null) {
                return@withContext Result.failure(IllegalStateException("Failed to render screen frame."))
            }

            withContext(Dispatchers.Default) {
                // Downscale for fast transmission
                val optimizedBitmap = scaleBitmapDown(bitmap, 1280)
                val thumbnailBitmap = scaleBitmapDown(bitmap, 240)
                val base64String = encodeBitmapToBase64(optimizedBitmap)

                Result.success(
                    CapturedScreenData(
                        fullBitmap = optimizedBitmap,
                        thumbnailBitmap = thumbnailBitmap,
                        base64Jpeg = base64String
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun convertImageToBitmap(image: Image, targetWidth: Int, targetHeight: Int): Bitmap? {
        return try {
            val plane = image.planes[0]
            val buffer = plane.buffer
            buffer.rewind()
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * targetWidth

            val rawBitmap = Bitmap.createBitmap(
                targetWidth + rowPadding / pixelStride,
                targetHeight,
                Bitmap.Config.ARGB_8888
            )
            rawBitmap.copyPixelsFromBuffer(buffer)

            if (rowPadding == 0) {
                rawBitmap
            } else {
                Bitmap.createBitmap(rawBitmap, 0, 0, targetWidth, targetHeight).also {
                    if (it != rawBitmap) {
                        rawBitmap.recycle()
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun scaleBitmapDown(src: Bitmap, maxDimension: Int): Bitmap {
        val width = src.width
        val height = src.height
        val maxSide = maxOf(width, height)

        if (maxSide <= maxDimension) {
            return src
        }

        val scale = maxDimension.toFloat() / maxSide
        val dstWidth = (width * scale).toInt().coerceAtLeast(1)
        val dstHeight = (height * scale).toInt().coerceAtLeast(1)

        return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, true)
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun teardownDisplay() {
        try {
            latestAcquiredImage?.close()
            latestAcquiredImage = null
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun release() {
        teardownDisplay()
        try {
            mediaProjection?.unregisterCallback(projectionCallback)
            mediaProjection?.stop()
        } catch (e: Exception) {
            // Ignore
        }
        mediaProjection = null
    }

    data class CapturedScreenData(
        val fullBitmap: Bitmap,
        val thumbnailBitmap: Bitmap,
        val base64Jpeg: String
    )
}
