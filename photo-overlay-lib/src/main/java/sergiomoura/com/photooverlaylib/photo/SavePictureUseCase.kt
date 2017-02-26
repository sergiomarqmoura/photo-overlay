package sergiomoura.com.photooverlaylib.photo

import android.graphics.Bitmap
import rx.Observable

open class SavePictureUseCase(private val photoManager: PhotoManager) {

    open fun build(cameraBitmap: Bitmap, overlaysBitmap: Bitmap): Observable<String?> {
        return photoManager.savePicture(cameraBitmap, overlaysBitmap)
    }
}