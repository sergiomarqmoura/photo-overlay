package sergiomoura.com.photooverlaylib.overlay

data class Overlay(val name: String, val resource: Int? = null) {
    var x = 0.0f
    var y = 0.0f

    val toChooseFromGallery: Boolean
        get() = resource == null
}