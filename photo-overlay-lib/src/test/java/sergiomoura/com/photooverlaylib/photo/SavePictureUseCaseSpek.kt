package sergiomoura.com.photooverlaylib.photo

import android.graphics.Bitmap
import com.nhaarman.mockito_kotlin.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import rx.Observable
import rx.observers.TestSubscriber
import java.io.IOException

@RunWith(JUnitPlatform::class)
class SavePictureUseCaseSpek : Spek({
    given("SavePictureUseCase") {
        val photoManager: PhotoManager = mock()
        val tested = SavePictureUseCase(photoManager)
        var testSubscriber: TestSubscriber<String?> = TestSubscriber()

        val savedPicturePath = "file:storage/picture1"

        val overlaysBitmap: Bitmap = mock()
        val photoBitmap: Bitmap = mock()

        beforeEach {
            reset(photoManager)

            testSubscriber = TestSubscriber()

            whenever(photoManager.savePicture(any(), any())).thenReturn(Observable.just(savedPicturePath))
        }

        describe("#build") {
            on("general case") {
                beforeEach { tested.build(photoBitmap, overlaysBitmap).subscribe(testSubscriber) }

                it("should call the photo manager to save the photo") {
                    verify(photoManager).savePicture(photoBitmap, overlaysBitmap)
                }
            }

            on("success saving photo to disk") {
                beforeEach { tested.build(photoBitmap, overlaysBitmap).subscribe(testSubscriber) }

                it("should return the saved photo") {
                    with(testSubscriber) {
                        assertValue(savedPicturePath)
                        assertNoErrors()
                        assertCompleted()
                    }
                }
            }

            on("error saving photo to disk") {
                beforeEach {
                    whenever(photoManager.savePicture(any(), any())).thenReturn(Observable.error(IOException()))
                    tested.build(photoBitmap, overlaysBitmap).subscribe(testSubscriber)
                }
                it("should return the exception") {
                    with(testSubscriber) {
                        assertError(IOException::class.java)
                        assertNoValues()
                    }
                }
            }
        }
    }
})