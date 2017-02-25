package sergiomoura.com.photooverlaylib.photo

import java.io.File

interface PictureAvailabilityListener {
    fun onPictureReady(picture: String?)
}