package ir.mehdiyari.fallery.buckets.bucketContent.content.adapter

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
import ir.mehdiyari.fallery.utils.BUCKET_CONTENT_DEFAULT_SPAN_COUNT
import ir.mehdiyari.fallery.utils.convertSecondToTime
import ir.mehdiyari.fallery.utils.dpToPx
import ir.mehdiyari.fallery.utils.getHeightBasedOnScaledWidth
import kotlinx.android.synthetic.main.media_photo_item.view.*
import kotlinx.android.synthetic.main.media_video_item.view.*

internal class BucketContentAdapter constructor(
    private val imageLoader: FalleryImageLoader,
    private val selectedDrawable: Drawable,
    private val deselectedDrawable: Drawable,
    private val placeHolderColor: Int
) : ListAdapter<Media, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<Media>() {
    override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean = oldItem.getMediaId() == newItem.getMediaId()
    override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean = oldItem == newItem
}) {
    var getItemViewWidth: (() -> (Int))? = null
    var selectedMediaTracker: MutableList<String>? = null
    var onMediaSelected: ((String) -> (Boolean))? = null
    var onMediaDeselected: ((String) -> (Boolean))? = null
    var onMediaClick: ((String) -> Unit)? = null
    var spanCount = BUCKET_CONTENT_DEFAULT_SPAN_COUNT

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        LayoutInflater.from(parent.context)
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

            setViewHeight()
        }

        private fun setViewHeight() {
            itemView.constraintLayoutPhotoContainer.also {
                it.layoutParams = it.layoutParams.apply {
                    this.height = if (spanCount > BUCKET_CONTENT_DEFAULT_SPAN_COUNT) {
                        internalGetItemViewWidth()
                    } else {
                        itemView.context.resources.getDimensionPixelSize(R.dimen.min_size_bucket_content_item)
                    }
                }
            }
        }

        fun bind() {
            getItem(adapterPosition).also { currentPhoto ->
                setViewHeight()
                if (currentPhoto is Media.Photo) {
                    initSelectingStateOfView(adapterPosition)
                    val dimension = PhotoDiminution(
                        internalGetItemViewWidth(),
                        getHeightBasedOnScaledWidth(
                            currentPhoto.width,
                            currentPhoto.height,
                            internalGetItemViewWidth()
                        )
                    )
                    imageLoader.loadPhoto(
                        context = itemView.context,
                        imageView = itemView.imageViewPhotoMedia,
                        resizeDiminution = dimension,
                        placeHolderColor = placeHolderColor,
                        path = currentPhoto.path
                    )
                }
            }
        }

        private fun initSelectingStateOfView(adapterPosition: Int) {
            if (selectedMediaTracker?.contains(getItemPath(adapterPosition)) == true)
                itemView.imageViewSelectDeselectPhoto.setImageDrawable(selectedDrawable)
            else
                itemView.imageViewSelectDeselectPhoto.setImageDrawable(deselectedDrawable)
        }

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

            setViewHeight()
        }

        private fun setViewHeight() {
            itemView.constraintLayoutVideoContainer.also {
                it.layoutParams = it.layoutParams.apply {
                    this.height = if (spanCount > BUCKET_CONTENT_DEFAULT_SPAN_COUNT) {
                        internalGetItemViewWidth()
                    } else {
                        itemView.context.resources.getDimensionPixelSize(R.dimen.min_size_bucket_content_item)
                    }
                }
            }
        }

        fun bind() {
            getItem(adapterPosition).also { currentVideo ->
                setViewHeight()
                if (currentVideo is Media.Video) {
                    val dimension =
                        PhotoDiminution(
                            internalGetItemViewWidth(),
                            getHeightBasedOnScaledWidth(
                                currentVideo.thumbnail.width,
                                currentVideo.thumbnail.height,
                                internalGetItemViewWidth()
                            )
                        )
                    imageLoader.loadPhoto(
                        context = itemView.context,
                        imageView = itemView.imageViewVideoMedia,
                        resizeDiminution = dimension,
                        placeHolderColor = placeHolderColor,
                        path = currentVideo.thumbnail.path
                    )

                    itemView.textViewVideoTime.text =
                        convertSecondToTime(currentVideo.duration.toInt())
                    initSelectingStateOfView(adapterPosition)
                }
            }
        }

        private fun initSelectingStateOfView(adapterPosition: Int) {
            if (selectedMediaTracker?.contains(getItemPath(adapterPosition)) == true)
                itemView.imageViewSelectDeselectVideo.setImageDrawable(selectedDrawable)
            else
                itemView.imageViewSelectDeselectVideo.setImageDrawable(deselectedDrawable)
        }
    }

    private fun internalGetItemViewWidth(): Int {
        return getItemViewWidth?.invoke() ?: dpToPx(200)
    }
}