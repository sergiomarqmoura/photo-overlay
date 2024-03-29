package sergiomoura.com.photooverlay

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import com.jakewharton.rxbinding.widget.RxCompoundButton
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import sergiomoura.com.photooverlay.common.FullscreenPhotoActivity
import sergiomoura.com.photooverlay.common.permissions.RuntimePermissionsManager
import sergiomoura.com.photooverlay.gallery.GalleryActivity
import sergiomoura.com.photooverlaylib.overlay.GalleryOverlay
import sergiomoura.com.photooverlaylib.overlay.Overlay
import sergiomoura.com.photooverlaylib.overlay.ResourceOverlay
import sergiomoura.com.photooverlaylib.photo.PictureAvailabilityListener
import sergiomoura.com.photooverlaylib.photo.PicturesListener
import sergiomoura.com.photooverlaylib.view.PhotoOverlayView

class MainActivity : AppCompatActivity() {

    private lateinit var overlayDialog: MaterialDialog
    private var possibleOverlays = listOf<Overlay>()

    private val runtimePermissionsManager by lazy { RuntimePermissionsManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setupViews()
    }

    override fun onStart() {
        super.onStart()
        runtimePermissionsManager.requestCameraAndStoragePermissions()
    }

    private fun setupViews() {
        RxCompoundButton.checkedChanges(toggleCameraStatus)
                .skip(1)
                .subscribe({ isChecked ->
                    if (isChecked) {
                        photoOverlayView.openCamera()
                    } else {
                        photoOverlayView.closeCamera()
                    }
                }, { Log.e("Error: ", "error: ${it.message}") })

        addOverlay.setOnClickListener {
            launchOverlayDialog()
        }

        clearOverlays.setOnClickListener {
            photoOverlayView.clearOverlays()
        }

        takePicture.setOnClickListener {
            photoOverlayView.takePicture(object : PictureAvailabilityListener {
                override fun onPictureReady(picture: String?) {
                    Picasso.with(this@MainActivity)
                            .load(picture)
                            .into(takenPhoto)

                    takenPhoto.setOnClickListener { FullscreenPhotoActivity.start(this@MainActivity, picture) }
                }

                override fun onErrorSavingPicture() {
                    // empty
                }
            })
        }

        gallery.setOnClickListener {
            photoOverlayView.getTakenPictures(object : PicturesListener {
                override fun onPicturesAvailable(pictures: List<String?>) {
                    GalleryActivity.start(this@MainActivity, pictures)
                }

                override fun onErrorFetchingPictures() {
                    // empty
                }
            })
        }

        possibleOverlays = listOf(
                ResourceOverlay(getString(R.string.dialog_moustache), R.drawable.moustache),
                ResourceOverlay(getString(R.string.dialog_hat), R.drawable.hat),
                ResourceOverlay(getString(R.string.dialog_glasses), R.drawable.glasses),
                Overlay(getString(R.string.dialog_gallery)))

        overlayDialog = MaterialDialog.Builder(this)
                .title(R.string.dialog_title)
                .items(possibleOverlays.map { it.name })
                .itemsCallbackSingleChoice(-1) { dialog, view, which, text ->
                    val chosenOverlay = possibleOverlays.find { it.name == "$text" }
                    when (chosenOverlay) {
                        is ResourceOverlay -> photoOverlayView.addResourceOverlay(chosenOverlay)
                        is Overlay -> photoOverlayView.launchGallery(this)
                    }

                    true
                }
                .positiveText(R.string.dialog_positive)
                .negativeText(R.string.dialog_negative)
                .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                PhotoOverlayView.GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data
                    photoOverlayView.addGalleryOverlay(GalleryOverlay(imageUri))
                }
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            RuntimePermissionsManager.PERMISSIONS_REQUEST_STORAGE_CAMERA -> {
                if (RuntimePermissionsManager.allPermissionsGranted(grantResults)) {
                    if (toggleCameraStatus.isChecked) {
                        photoOverlayView.openCamera()
                    }
                } else {
                    finish()
                }
            }
        }
    }

    private fun launchOverlayDialog() {
        overlayDialog.show()
    }
}
