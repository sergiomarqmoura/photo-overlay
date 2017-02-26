package sergiomoura.com.photooverlaylib.photo

import rx.Observable

open class GetTakenPhotosUseCase(private val photoManager: PhotoManager) {

    open fun build(): Observable<List<String?>> {
        return photoManager.getAllPictures()
            .toList()
    }
}