package sergiomoura.com.photooverlaylib.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.android.synthetic.main.photo_overlay_view.view.*
import sergiomoura.com.photooverlaylib.R
import sergiomoura.com.photooverlaylib.common.di.DaggerLibComponent
import sergiomoura.com.photooverlaylib.common.di.LibModule
import sergiomoura.com.photooverlaylib.overlay.GalleryOverlay
import sergiomoura.com.photooverlaylib.overlay.ResourceOverlay
import sergiomoura.com.photooverlaylib.photo.CameraInteractor
import sergiomoura.com.photooverlaylib.photo.PictureAvailabilityListener
import sergiomoura.com.photooverlaylib.photo.PicturesListener
import javax.inject.Inject

class PhotoOverlayView : FrameLayout, PhotoOverlayPresenter.View {

    private val currentOverlays: MutableList<ImageView> = mutableListOf()

    @Inject
    internal lateinit var presenter: PhotoOverlayPresenter

    private val cameraInteractor by lazy { CameraInteractor() }

    private var pictureAvailabilityListener: PictureAvailabilityListener? = null
    private var picturesListener: PicturesListener? = null

    private val textureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            cameraInteractor.openCamera(context, preview.surfaceTexture, width, height)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            // empty
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            cameraInteractor.closeCamera()
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            // empty
        }
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

        DaggerLibComponent.builder()
            .libModule(LibModule())
            .build()
            .inject(this)

        presenter.attachView(this)
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

    override fun onErrorFetchingPictures() {
        picturesListener?.onErrorFetchingPictures()
    }

    override fun onErrorSavingPicture() {
        pictureAvailabilityListener?.onErrorSavingPicture()
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

    fun openCamera() {
        if (preview.isAvailable) {
            cameraInteractor.openCamera(context, preview.surfaceTexture, width, height)
        } else {
            preview.surfaceTextureListener = textureListener
        }
    }

    fun closeCamera() {
        cameraInteractor.closeCamera()
        preview.surfaceTextureListener = null
    }

    companion object {
        private const val DEFAULT_OVERLAY_WIDTH = 450
        private const val DEFAULT_OVERLAY_HEIGHT = 450
        const val GALLERY_REQUEST_CODE = 21
    }
}