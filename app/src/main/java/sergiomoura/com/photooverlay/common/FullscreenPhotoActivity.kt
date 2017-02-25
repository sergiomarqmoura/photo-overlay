package sergiomoura.com.photooverlay.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_fullscreen_photo.*
import sergiomoura.com.photooverlay.R

/**
 * A example full-screen activity with a touch-capable image.
 */
class FullscreenPhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen_photo)

        val photoUrl = intent.getStringExtra(PHOTO_URL_EXTRA)

        Picasso.with(this).load(photoUrl).into(image)
    }

    companion object {
        const val PHOTO_URL_EXTRA = "PHOTO_URL"

        fun start(context: Context, photoUrl: String?) {
            val intent = Intent(context, FullscreenPhotoActivity::class.java)
                .putExtra(PHOTO_URL_EXTRA, photoUrl)
            context.startActivity(intent)
        }
    }
}
