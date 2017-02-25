package sergiomoura.com.photooverlaylib.photo

import java.io.File

interface PicturesListener {
    fun onPicturesAvailable(pictures: List<String>)
}