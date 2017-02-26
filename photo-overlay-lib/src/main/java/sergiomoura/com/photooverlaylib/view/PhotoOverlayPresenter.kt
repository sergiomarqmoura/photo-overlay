package sergiomoura.com.photooverlaylib.view

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import sergiomoura.com.photooverlaylib.photo.GetTakenPhotosUseCase
import sergiomoura.com.photooverlaylib.photo.SavePictureUseCase
import java.io.FileNotFoundException

internal class PhotoOverlayPresenter(private val getTakenPhotosUseCase: GetTakenPhotosUseCase,
                                     private val savePictureUseCase: SavePictureUseCase) {

    private lateinit var view: View

    private val subscriptions by lazy { CompositeSubscription() }

    fun attachView(view: View) {
        this.view = view
    }

    fun getTakenPictures() {
        subscriptions.add(getTakenPhotosUseCase.build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<List<String?>> {
                    override fun onError(e: Throwable) {
                        view.onErrorFetchingPictures()
                    }

                    override fun onNext(pictures: List<String?>) {
                        view.setTakenPictures(pictures)
                    }

                    override fun onCompleted() {
                        // empty
                    }
                }))
    }

    fun getDrawableFromUri(contentResolver: ContentResolver, photoUri: Uri): Drawable? {
        try {
            val inputStream = contentResolver.openInputStream(photoUri)
            return Drawable.createFromStream(inputStream, photoUri.toString())
        } catch (e: FileNotFoundException) {
            return null
        }
    }

    fun takePicture(cameraBitmap: Bitmap, overlaysBitmap: Bitmap) {
        subscriptions.add(savePictureUseCase.build(cameraBitmap, overlaysBitmap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String?> {
                    override fun onError(e: Throwable) {
                        view.onErrorSavingPicture()
                    }

                    override fun onNext(picture: String?) {
                        view.setPicture(picture)
                    }

                    override fun onCompleted() {
                        // empty
                    }
                }))
    }

    fun stop() {
        subscriptions.clear()
    }

    interface View {
        fun setPicture(picture: String?)
        fun setTakenPictures(pictures: List<String?>)
        fun onErrorFetchingPictures()
        fun onErrorSavingPicture()
    }
}