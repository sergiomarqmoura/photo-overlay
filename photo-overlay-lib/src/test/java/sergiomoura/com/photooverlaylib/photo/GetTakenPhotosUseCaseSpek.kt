package sergiomoura.com.photooverlaylib.photo

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
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
class GetTakenPhotosUseCaseSpek : Spek({
    given("GetTakenPhotosUseCase") {
        val photoManager: PhotoManager = mock()
        val tested = GetTakenPhotosUseCase(photoManager)
        var testSubscriber: TestSubscriber<List<String?>> = TestSubscriber()

        val picturePaths = listOf(
                "file:storage/picture1",
                "file:storage/picture2")

        beforeEach {
            reset(photoManager)

            testSubscriber = TestSubscriber()

            whenever(photoManager.getAllPictures()).thenReturn(Observable.from(picturePaths))
        }

        describe("#build") {
            on("general case") {
                beforeEach { tested.build().subscribe(testSubscriber) }

                it("should call the photo manager to fetch the photos") {
                    verify(photoManager).getAllPictures()
                }
            }

            on("success getting photos from disk") {
                beforeEach { tested.build().subscribe(testSubscriber) }

                it("should return the taken photos") {
                    with(testSubscriber) {
                        assertValues(picturePaths)
                        assertNoErrors()
                        assertCompleted()
                    }
                }
            }

            on("error getting photos from disk") {
                beforeEach {
                    whenever(photoManager.getAllPictures()).thenReturn(Observable.error(IOException()))
                    tested.build().subscribe(testSubscriber)
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