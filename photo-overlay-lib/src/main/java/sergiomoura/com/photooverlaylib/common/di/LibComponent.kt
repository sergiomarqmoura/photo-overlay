package sergiomoura.com.photooverlaylib.common.di

import dagger.Component
import sergiomoura.com.photooverlaylib.view.PhotoOverlayView
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(LibModule::class))
interface LibComponent {

    fun inject(view: PhotoOverlayView)
}