package sergiomoura.com.photooverlaylib.common.di

import dagger.Module
import dagger.Provides
import sergiomoura.com.photooverlaylib.photo.GetTakenPhotosUseCase
import sergiomoura.com.photooverlaylib.photo.PhotoManager
import sergiomoura.com.photooverlaylib.photo.SavePictureUseCase
import sergiomoura.com.photooverlaylib.view.PhotoOverlayPresenter

@Module
class LibModule {

    @Provides
    fun provideGetTakenPhotosUseCase(photoManager: PhotoManager): GetTakenPhotosUseCase {
        return GetTakenPhotosUseCase(photoManager)
    }

    @Provides
    fun provideSavePictureUseCase(photoManager: PhotoManager): SavePictureUseCase {
        return SavePictureUseCase(photoManager)
    }

    @Provides
    internal fun providePhotoViewPresenter(getTakenPhotosUseCase: GetTakenPhotosUseCase,
                                  savePictureUseCase: SavePictureUseCase): PhotoOverlayPresenter {
        return PhotoOverlayPresenter(getTakenPhotosUseCase, savePictureUseCase)
    }

    @Provides
    fun providePhotoManager(): PhotoManager {
        return PhotoManager()
    }
}