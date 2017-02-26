package sergiomoura.com.photooverlaylib.photo

interface PictureAvailabilityListener {
    fun onPictureReady(picture: String?)
    fun onErrorSavingPicture()
}