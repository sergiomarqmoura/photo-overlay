package sergiomoura.com.photooverlaylib.view

import android.graphics.Bitmap
import com.nhaarman.mockito_kotlin.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import rx.Observable
import sergiomoura.com.photooverlaylib.photo.GetTakenPhotosUseCase
import sergiomoura.com.photooverlaylib.photo.SavePictureUseCase
import sergiomoura.com.photooverlaylib.rxGroup
import java.io.IOException

@RunWith(JUnitPlatform::class)
class PhotoOverlayPresenterSpek : Spek ({
    rxGroup("PhotoOverlayPresenter") {

        val getPicturesUseCase: GetTakenPhotosUseCase = mock()
        val savePictureUseCase: SavePictureUseCase = mock()

        val picturePaths = listOf(
                "file:storage/picture1",
                "file:storage/picture2")

        val savedPicturePath = "file:storage/picture1"

        val overlaysBitmap: Bitmap = mock()
        val photoBitmap: Bitmap = mock()

        val view: PhotoOverlayPresenter.View = mock()

        val tested = PhotoOverlayPresenter(getPicturesUseCase, savePictureUseCase)

        beforeEach {
            reset(getPicturesUseCase)
            reset(savePictureUseCase)
            reset(view)

            whenever(getPicturesUseCase.build()).thenReturn(Observable.just(picturePaths))
            whenever(savePictureUseCase.build(any(), any())).thenReturn(Observable.just(savedPicturePath))

            tested.attachView(view)
        }

        describe("#getTakenPictures") {
            context("pictures available") {
                beforeEach { tested.getTakenPictures() }

                it("should fetch the taken pictures") {
                    verify(getPicturesUseCase).build()
                }

                it("should return the fetched pictures") {
                    verify(view).setTakenPictures(picturePaths)
                }
            }

            context("error fetching available pictures") {
                beforeEach {
                    whenever(getPicturesUseCase.build()).thenReturn(Observable.error(IOException()))
                    tested.getTakenPictures()
                }

                it("should return the error") {
                    verify(view).onErrorFetchingPictures()
                }
            }
        }

        describe("#takePicture") {
            context("pictures available") {
                beforeEach { tested.takePicture(photoBitmap, overlaysBitmap) }

                it("should try to save the picture") {
                    verify(savePictureUseCase).build(photoBitmap, overlaysBitmap)
                }

                it("should return the saved picture") {
                    verify(view).setPicture(savedPicturePath)
                }
            }

            context("error saving picture") {
                beforeEach {
                    whenever(savePictureUseCase.build(any(), any())).thenReturn(Observable.error(IOException()))
                    tested.takePicture(photoBitmap, overlaysBitmap)
                }

                it("should return the error") {
                    verify(view).onErrorSavingPicture()
                }
            }
        }
    }
})