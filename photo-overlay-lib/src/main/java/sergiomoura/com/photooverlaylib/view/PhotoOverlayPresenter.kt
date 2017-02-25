package sergiomoura.com.photooverlaylib.view

import android.graphics.Bitmap
import android.util.Log
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import sergiomoura.com.photooverlaylib.photo.PhotoManager
import java.io.File

internal class PhotoOverlayPresenter {

    private lateinit var view: View

    private val photoManager by lazy { PhotoManager() }

    private val subscriptions by lazy { CompositeSubscription() }

    fun attachView(view: View) {
        this.view = view
    }

    fun getTakenPictures() {
        subscriptions.add(photoManager.getAllPictures()
                .map { it.path }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<List<String>> {
                    override fun onError(e: Throwable) {
                        Log.e("PhotoOverlayPresenter", "Error fetching saved pictures: ${e.message}")
                        view.setPicture(null)
                    }

                    override fun onNext(pictures: List<String>) {
                        view.setTakenPictures(pictures)
                    }

                    override fun onCompleted() {
                        // empty
                    }
                }))

    }

    fun takePicture(cameraBitmap: Bitmap, overlaysBitmap: Bitmap) {
        subscriptions.add(photoManager.savePicture(cameraBitmap, overlaysBitmap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<File?> {
                    override fun onError(e: Throwable) {
                        Log.e("PhotoOverlayPresenter", "Error saving picture: ${e.message}")
                        view.setPicture(null)
                    }

                    override fun onNext(picture: File?) {
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
        fun setPicture(picture: File?)
        fun setTakenPictures(pictures: List<String>)
    }
}