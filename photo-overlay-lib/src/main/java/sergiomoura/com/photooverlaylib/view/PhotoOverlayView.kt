package sergiomoura.com.photooverlaylib.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.support.annotation.DrawableRes
import android.support.annotation.NonNull
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.photo_overlay_view.view.*
import sergiomoura.com.photooverlaylib.R
import sergiomoura.com.photooverlaylib.overlay.GalleryOverlay
import sergiomoura.com.photooverlaylib.overlay.ResourceOverlay
import sergiomoura.com.photooverlaylib.photo.PictureAvailabilityListener
import sergiomoura.com.photooverlaylib.photo.PicturesListener
import java.util.*

class PhotoOverlayView : FrameLayout, PhotoOverlayPresenter.View {

    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null

    private val currentOverlays: MutableList<ImageView> = mutableListOf()

    private val presenter by lazy { PhotoOverlayPresenter() }

    private var pictureAvailabilityListener: PictureAvailabilityListener? = null
    private var picturesListener: PicturesListener? = null

    private val textureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {

        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            // Transform you image captured size according to the surface width and height
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            closeCamera()
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            //This is called when the camera is open
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    fun addResourceOverlay(overlay: ResourceOverlay) {
        val drawable = ContextCompat.getDrawable(context, overlay.resource)
        val overlayImage = createOverlayImage(drawable)
        currentOverlays.add(overlayImage)
        addView(overlayImage)
    }

    fun addGalleryOverlay(overlay: GalleryOverlay) {
        overlay.resource?.let {
            val drawable = presenter.getDrawableFromUri(context.contentResolver, it)
            drawable?.let {
                val overlayImage = createOverlayImage(it)
                currentOverlays.add(overlayImage)
                addView(overlayImage)
            }
        }
    }

    private fun createOverlayImage(resource: Drawable): ImageView {
        return ImageView(context).apply {
            setImageDrawable(resizeDrawable(resource))
            val imageViewParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                    ViewGroup.MarginLayoutParams.WRAP_CONTENT)
            layoutParams = imageViewParams
            setOnTouchListener { view, motionEvent ->
                val positionX = motionEvent.rawX
                val positionY = motionEvent.rawY
                when (motionEvent.action.and(MotionEvent.ACTION_MASK)) {
                    MotionEvent.ACTION_MOVE -> {
                        view.layoutParams.apply {
                            x = positionX - view.width
                            y = positionY - view.height
                        }
                    }
                }
                true
            }
        }
    }

    fun getTakenPictures(picturesListener: PicturesListener) {
        this.picturesListener = picturesListener
        presenter.getTakenPictures()
    }

    override fun setTakenPictures(pictures: List<String?>) {
        picturesListener?.onPicturesAvailable(pictures)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        presenter.stop()
    }

    fun clearOverlays() {
        currentOverlays.forEach { removeView(it) }
    }

    fun takePicture(pictureAvailabilityListener: PictureAvailabilityListener) {
        this.pictureAvailabilityListener = pictureAvailabilityListener
        val cameraImage = preview.bitmap
        val overlaysImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        //Draw the view inside the Bitmap
        draw(Canvas(overlaysImage))

        presenter.takePicture(cameraImage, overlaysImage)
    }

    fun launchGallery(context: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        context.startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun setPicture(picture: String?) {
        pictureAvailabilityListener?.onPictureReady(picture)
    }

    private fun resizeDrawable(image: Drawable): Drawable {
        val originalBitmap = (image as BitmapDrawable).bitmap
        val bitmapResized = Bitmap.createScaledBitmap(originalBitmap, DEFAULT_OVERLAY_WIDTH, DEFAULT_OVERLAY_HEIGHT, false)
        return BitmapDrawable(resources, bitmapResized)
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.photo_overlay_view, this, true)

        preview.surfaceTextureListener = textureListener
        presenter.attachView(this)
    }

    private fun updatePreview() {
        if (null == cameraDevice) {
            Log.e("PhotoOverlayView", "updatePreview error, return")
        }
        captureRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSession?.setRepeatingRequest(captureRequestBuilder?.build(), null, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e("PhotoOverlayView", "Error accessing the camera: ${e.message}")
        }
    }

    fun toggleCameraStatus() {
        if (mBackgroundThread == null) {
            startBackgroundThread()
            if (preview.isAvailable) {
                openCamera()
            } else {
                preview.surfaceTextureListener = textureListener
            }
        } else {
            closeCamera()
        }
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background").apply {
            start()
            mBackgroundHandler = Handler(looper)
        }
    }

    private fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun openCamera() {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.e("PhotoOverlayView", "is camera open")
        try {
            val cameraId = manager.cameraIdList[1]
            // Add permission for camera and let user grant the permission
            manager.openCamera(cameraId, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        Log.e("PhotoOverlayView", "openCamera")
    }

    private fun closeCamera() {
        stopBackgroundThread()
        cameraDevice?.let {
            it.close()
            cameraDevice = null
        }
    }

    private fun createCameraPreview() {
        try {
            val texture = preview.surfaceTexture
            texture.setDefaultBufferSize(width, height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(surface)
            cameraDevice?.createCaptureSession(Arrays.asList(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
                    //The camera is already closed
                    if (cameraDevice == null) {
                        return
                    }
                    // When the session is ready, we start displaying the preview.
                    this@PhotoOverlayView.cameraCaptureSession = cameraCaptureSession
                    updatePreview()
                }

                override fun onConfigureFailed(@NonNull cameraCaptureSession: CameraCaptureSession) {
                    Toast.makeText(this@PhotoOverlayView.context, "Configuration change", Toast.LENGTH_SHORT).show()
                }
            }, null)
        } catch (e: CameraAccessException) {
            Log.e("PhotoOverlayView", "Error accessing camera: ${e.message}")
        }
    }

    companion object {
        private const val DEFAULT_OVERLAY_WIDTH = 450
        private const val DEFAULT_OVERLAY_HEIGHT = 450
        const val GALLERY_REQUEST_CODE = 21
    }
}