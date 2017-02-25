package sergiomoura.com.photooverlay.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.elem_gallery_photo.view.*
import sergiomoura.com.photooverlay.R
import java.io.File
import java.util.*

/**
 * Adapter used to handle the holes list grid view
 */
class GalleryAdapter(private val context: Context,
                     private val photoClickListener: PhotoClickListener) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private var photosList: List<String> = ArrayList()

    private val picasso = Picasso.with(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val photoPath = photosList[position]
        val photoDimension = context.resources.getDimensionPixelSize(R.dimen.gallery_photo_width)
        return inflater.inflate(R.layout.elem_gallery_photo, null).apply {
            with(photo) {
                picasso.load(File(photoPath))
                        .resize(photoDimension, photoDimension)
                        .centerCrop()
                        .into(this)
            }
            setOnClickListener {
                photoClickListener.onPhotoClick(photoPath)
            }
        }
    }

    override fun getItem(position: Int) = photosList[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = photosList.size

    fun setPhotosList(photoPaths: List<String>) {
        this.photosList = photoPaths
        notifyDataSetChanged()
    }
}