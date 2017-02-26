package sergiomoura.com.photooverlaylib.photo

interface PicturesListener {
    fun onPicturesAvailable(pictures: List<String?>)
    fun onErrorFetchingPictures()
}