package ir.mehdiyari.fallery.buckets.ui.bucketContent.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.imageLoader.FalleryImageLoader
import ir.mehdiyari.fallery.imageLoader.PhotoDiminution
import ir.mehdiyari.fallery.models.Media
import ir.mehdiyari.fallery.utils.convertSecondToTime
import ir.mehdiyari.fallery.utils.dpToPx
import ir.mehdiyari.fallery.utils.getHeightBasedOnScaledWidth
import kotlinx.android.synthetic.main.media_photo_item.view.*
import kotlinx.android.synthetic.main.media_video_item.view.*

class BucketContentAdapter constructor(
    private val imageLoader: FalleryImageLoader,
    private val selectedDrawable: Drawable,
    private val deselectedDrawable: Drawable
) : ListAdapter<Media, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<Media>() {
    override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean = oldItem.getMediaId() == newItem.getMediaId()
    override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean = oldItem == newItem
}) {

    private var itemViewWidth: Int = 0
    var getItemViewWidth: (() -> (Int))? = null
    var selectedMediaTracker: MutableList<String>? = null
    var onMediaSelected: ((String) -> (Boolean))? = null
    var onMediaDeselected: ((String) -> (Boolean))? = null
    var onMediaClick: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = LayoutInflater.from(parent.context)
        .inflate(viewType, parent, false).let {
            when (viewType) {
                R.layout.media_photo_item -> PhotoViewHolder(it)
                R.layout.media_video_item -> VideoViewHolder(it)
                else -> throw IllegalArgumentException("bad viewType")
            }
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PhotoViewHolder -> holder.bind()
            is VideoViewHolder -> holder.bind()
        }
    }

    override fun getItemViewType(position: Int): Int = when (currentList[position]) {
        is Media.Video -> R.layout.media_video_item
        is Media.Photo -> R.layout.media_photo_item
    }

    override fun getItemId(position: Int): Long = currentList[position].getMediaId()
    private fun getItemPath(position: Int): String = currentList[position].getMediaPath()


    private fun onClickSelectingToggle(adapterPosition: Int, onSuccess: (() -> (Unit))) {
        val path = getItemPath(adapterPosition)
        if ((if (selectedMediaTracker?.contains(path) == true)
                onMediaDeselected?.invoke(path)
            else
                onMediaSelected?.invoke(path)) == true
        ) onSuccess()
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.selectedPhotoToggleContainer.setOnClickListener {
                onClickSelectingToggle(adapterPosition) {
                    initSelectingStateOfView(adapterPosition)
                }
            }

            itemView.imageViewPhotoMedia.setOnClickListener {
                onMediaClick?.invoke(getItemPath(adapterPosition))
            }
        }

        fun bind() {
            getItem(adapterPosition).also { currentPhoto ->
                if (currentPhoto is Media.Photo) {
                    initSelectingStateOfView(adapterPosition)
                    val dimension = PhotoDiminution(getItemViewWidth(), getHeightBasedOnScaledWidth(currentPhoto.width, currentPhoto.height, getItemViewWidth()))
                    imageLoader.loadPhoto(
                        context = itemView.context,
                        imageView = itemView.imageViewPhotoMedia,
                        resizeDiminution = dimension,
                        path = currentPhoto.path
                    )
                }
            }
        }

        private fun initSelectingStateOfView(adapterPosition: Int) {
            if (selectedMediaTracker?.contains(getItemPath(adapterPosition)) == true)
                itemView.imageViewSelectDeselectPhoto.background = selectedDrawable
            else
                itemView.imageViewSelectDeselectPhoto.background = deselectedDrawable
        }

    }

    private fun getItemViewWidth(): Int {
        if (itemViewWidth == 0)
            itemViewWidth = getItemViewWidth?.invoke() ?: dpToPx(200)

        return itemViewWidth
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.selectedVideoToggleContainer.setOnClickListener {
                onClickSelectingToggle(adapterPosition) {
                    initSelectingStateOfView(adapterPosition)
                }
            }

            itemView.imageViewVideoMedia.setOnClickListener {
                onMediaClick?.invoke(getItemPath(adapterPosition))
            }
        }

        fun bind() {
            getItem(adapterPosition).also { currentVideo ->
                if (currentVideo is Media.Video) {
                    val dimension =
                        PhotoDiminution(getItemViewWidth(), getHeightBasedOnScaledWidth(currentVideo.thumbnail.width, currentVideo.thumbnail.height, getItemViewWidth()))
                    imageLoader.loadPhoto(
                        context = itemView.context,
                        imageView = itemView.imageViewVideoMedia,
                        resizeDiminution = dimension,
                        path = currentVideo.path
                    )

                    itemView.textViewVideoTime.text = convertSecondToTime(currentVideo.duration.toInt())
                    initSelectingStateOfView(adapterPosition)
                }
            }
        }

        private fun initSelectingStateOfView(adapterPosition: Int) {
            if (selectedMediaTracker?.contains(getItemPath(adapterPosition)) == true)
                itemView.imageViewSelectDeselectVideo.background = selectedDrawable
            else
                itemView.imageViewSelectDeselectVideo.background = deselectedDrawable
        }
    }
}