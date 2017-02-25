package sergiomoura.com.photooverlay.gallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_gallery.*
import sergiomoura.com.photooverlay.R
import java.util.*

class GalleryActivity : AppCompatActivity() {

    private val galleryAdapter by lazy { GalleryAdapter(this, photoClickListener) }

    private val photoClickListener = object : PhotoClickListener {
        override fun onPhotoClick(path: String) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val photos = intent.getStringArrayListExtra(PHOTOS_LIST_EXTRA)

        with(galleryAdapter) {
            galleryGrid.adapter = this
            setPhotosList(photos)
        }
    }

    companion object {
        const val PHOTOS_LIST_EXTRA = "PHOTOS_LIST"
        fun start(context: Context, pictures: List<String>) {
            val intent = Intent(context, GalleryActivity::class.java)
            intent.putStringArrayListExtra(PHOTOS_LIST_EXTRA, ArrayList(pictures))
            context.startActivity(intent)
        }
    }
}