package sergiomoura.com.photooverlaylib.photo

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.NonNull
import android.util.Log
import android.view.Surface
import java.util.*

class CameraInteractor {

    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var surfaceViewWidth: Int = 0
    private var surfaceViewHeight: Int = 0

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            surfaceTexture?.let {
                createCameraPreview(it, surfaceViewWidth, surfaceViewHeight)
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    private fun createCameraPreview(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        try {
            surfaceTexture.setDefaultBufferSize(width, height)
            val surface = Surface(surfaceTexture)
            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(surface)
            cameraDevice?.createCaptureSession(Arrays.asList(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
                    cameraDevice?.let {
                        // When the session is ready, we start displaying the preview.
                        this@CameraInteractor.cameraCaptureSession = cameraCaptureSession
                        updatePreview()
                    }
                }

                override fun onConfigureFailed(@NonNull cameraCaptureSession: CameraCaptureSession) {
                    // empty
                }
            }, null)
        } catch (e: CameraAccessException) {
            Log.e("PhotoOverlayView", "Error accessing camera: ${e.message}")
        }
    }

    private fun launchCamera(context: Context) {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = manager.cameraIdList[1]
            // Add permission for camera and let user grant the permission
            manager.openCamera(cameraId, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun closeCamera() {
        if (isCameraOpen()) {
            stopBackgroundThread()
            cameraDevice?.let {
                it.close()
                cameraDevice = null
            }
        }
    }

    private fun isCameraOpen() = backgroundThread != null

    fun openCamera(context: Context, surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            this.surfaceTexture = surfaceTexture
            surfaceViewWidth = width
            surfaceViewHeight = height

            startBackgroundThread()
            launchCamera(context)
    }

    private fun updatePreview() {
        cameraDevice?.let {
            captureRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            try {
                cameraCaptureSession?.setRepeatingRequest(captureRequestBuilder?.build(), null, backgroundHandler)
            } catch (e: CameraAccessException) {
                Log.e("PhotoOverlayView", "Error accessing the camera: ${e.message}")
            }
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread(BACKGROUND_THREAD_TAG).apply {
            start()
            backgroundHandler = Handler(looper)
        }
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val BACKGROUND_THREAD_TAG = "Camera Background"
    }
}