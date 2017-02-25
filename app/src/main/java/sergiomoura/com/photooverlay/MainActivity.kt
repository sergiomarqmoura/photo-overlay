package sergiomoura.com.photooverlay

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import sergiomoura.com.photooverlay.gallery.GalleryActivity
import sergiomoura.com.photooverlaylib.overlay.Overlay
import sergiomoura.com.photooverlaylib.photo.PictureAvailabilityListener
import sergiomoura.com.photooverlaylib.photo.PicturesListener
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var overlayDialog: MaterialDialog
    private var possibleOverlays = listOf<Overlay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
    }

    private fun setupViews() {
        toggleCameraStatus.setOnClickListener {
            photoOverlayView.toggleCameraStatus()
        }

        addOverlay.setOnClickListener {
            launchOverlayDialog()
        }

        clearOverlays.setOnClickListener {
            photoOverlayView.clearOverlays()
        }

        takePicture.setOnClickListener {
            photoOverlayView.takePicture(object : PictureAvailabilityListener {
                override fun onPictureReady(picture: File?) {
                    Picasso.with(this@MainActivity)
                            .load(picture)
                            .into(takenPhoto)
                }
            })
        }

        gallery.setOnClickListener {
            photoOverlayView.getTakenPictures(object : PicturesListener {
                override fun onPicturesAvailable(pictures: List<String>) {
                    Log.d("Got pictures!", "Pictures: ${pictures.size}")
                    GalleryActivity.start(this@MainActivity, pictures)
                }
            })
        }

        possibleOverlays = listOf(
                Overlay(getString(R.string.dialog_moustache), R.drawable.moustache),
                Overlay(getString(R.string.dialog_hat), R.drawable.hat),
                Overlay(getString(R.string.dialog_glasses), R.drawable.glasses),
                Overlay(getString(R.string.dialog_gallery)))

        overlayDialog = MaterialDialog.Builder(this)
                .title(R.string.dialog_title)
                .items(possibleOverlays.map { it.name })
                .itemsCallbackSingleChoice(-1) { dialog, view, which, text ->
                    possibleOverlays.find { it.name == "$text" }?.let {
                        photoOverlayView.addOverlay(it)
                    }
                    true
                }
                .positiveText(R.string.dialog_positive)
                .negativeText(R.string.dialog_negative)
                .build()
    }

    private fun launchOverlayDialog() {
        overlayDialog.show()
    }
}
