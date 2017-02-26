package sergiomoura.com.photooverlaylib.photo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Environment
import rx.Observable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

open class PhotoManager {

    companion object {
        private const val PHOTO_OVERLAY_IMAGE_STORAGE_PREFIX = "photo_overlay_"
        private const val FILE_URI_PREFIX = "file:"
        private const val DEFAULT_IMAGE_QUALITY = 90
    }

    private val matrix by lazy { Matrix() }

    open fun savePicture(cameraBitmap: Bitmap, overlaysBitmap: Bitmap): Observable<String?> {
        val finalBitmap = overlay(cameraBitmap, overlaysBitmap)
        val filename = "$PHOTO_OVERLAY_IMAGE_STORAGE_PREFIX${Calendar.getInstance().timeInMillis}"
        var out: FileOutputStream? = null
        var myFile: File? = null
        try {
            val path = Environment.getExternalStorageDirectory().toString()
            myFile = File(path, filename)
            out = FileOutputStream(myFile)

            finalBitmap.compress(Bitmap.CompressFormat.PNG, DEFAULT_IMAGE_QUALITY, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
                cameraBitmap.recycle()
                overlaysBitmap.recycle()
                finalBitmap.recycle()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return Observable.just(myFile.toUri())
    }

    private fun File?.toUri() = this?.let { "$FILE_URI_PREFIX$path" } ?: null

    open fun getAllPictures(): Observable<String?> {
        val storage = File(Environment.getExternalStorageDirectory().toString())
        return Observable.from(storage.listFiles { file, string ->
            string.startsWith(PHOTO_OVERLAY_IMAGE_STORAGE_PREFIX)
        }
                .map { it.toUri() })
    }

    private fun overlay(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
        val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(bmp1, matrix, null)
        canvas.drawBitmap(bmp2, 0f, 0f, null)
        return bmOverlay
    }
}