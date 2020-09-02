package ir.mehdiyari.fallery.buckets.bucketList.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.imageLoader.FalleryImageLoader
import ir.mehdiyari.fallery.imageLoader.PhotoDiminution
import ir.mehdiyari.fallery.models.MediaBucket
import ir.mehdiyari.fallery.utils.getPhotoDimension
import ir.mehdiyari.fallery.utils.toReadableCount

internal class BucketListAdapter constructor(
    mediaBucketDiffCallback: MediaBucketDiffCallback,
    private val imageLoader: FalleryImageLoader,
    private val placeHolderColor: Int
) : ListAdapter<MediaBucket, BucketListAdapter.BucketViewHolder>(mediaBucketDiffCallback) {

    var onBucketClick: ((bucketId: Long) -> (Unit))? = null
    var getImageViewWidth: (() -> Int)? = null
    private var imageViewSizeForScalingImages = 0

    init {
        setHasStableIds(true)
    }

    @LayoutRes
    var viewHolderId: Int = R.layout.grid_bucket_item_view

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BucketViewHolder =
        BucketViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))

    override fun onBindViewHolder(holder: BucketViewHolder, position: Int) = holder.bind()

    override fun getItemId(position: Int): Long = currentList[position].id

    private fun getPhotoDimensionBasedOnRecyclerViewBucketImageContainerSize(): PhotoDiminution {
        if (imageViewSizeForScalingImages == 0)
            imageViewSizeForScalingImages = getImageViewWidth?.invoke() ?: 0

        return PhotoDiminution(imageViewSizeForScalingImages, imageViewSizeForScalingImages)
    }

    inner class BucketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView.findViewById<AppCompatImageView>(R.id.imageViewBucketImage)
        private val appCompatTextViewBucketName = itemView.findViewById<AppCompatTextView>(R.id.appCompatTextViewBucketName)
        private val appCompatTextViewBucketItemCount = itemView.findViewById<AppCompatTextView>(R.id.appCompatTextViewBucketItemCount)

        init {
            itemView.findViewById<ConstraintLayout>(R.id.constraintLayoutBucketItemView).setOnClickListener {
                onBucketClick?.invoke(getItemId(adapterPosition))
            }
        }

        fun bind() {
            itemView.apply {
                currentList[adapterPosition].also { currentBucket ->
                    getPhotoDimensionBasedOnRecyclerViewBucketImageContainerSize().also { dimension ->
                        val originalDim = getPhotoDimension(currentBucket.firstMediaThumbPath)
                        val widthScale = originalDim.width / dimension.width.toFloat()
                        val newHeight = originalDim.height / widthScale
                        if (dimension.isNotSet())
                            imageLoader.loadPhoto(
                                context = this.context,
                                imageView = imageView,
                                resizeDiminution = getDefaultDimension(this.context),
                                placeHolderColor = placeHolderColor,
                                path = currentBucket.firstMediaThumbPath
                            )
                        else
                            imageLoader.loadPhoto(
                                context = this.context,
                                imageView = imageView,
                                resizeDiminution = dimension.copy(height = newHeight.toInt()),
                                placeHolderColor = placeHolderColor,
                                path = currentBucket.firstMediaThumbPath
                            )
                    }
                    appCompatTextViewBucketName.text = currentBucket.displayName
                    appCompatTextViewBucketItemCount.text = setMediaCountBasedOnLayout(currentBucket.mediaCount)
                }
            }
        }

        private fun setMediaCountBasedOnLayout(mediaCount: Int): String = when (viewHolderId) {
            R.layout.linear_bucket_item_view -> this@BucketViewHolder.itemView.context.getString(R.string.media_count, mediaCount)
            else -> mediaCount.toReadableCount()
        }

        private fun getDefaultDimension(context: Context): PhotoDiminution = PhotoDiminution(
            context.resources.getDimension(R.dimen.fallery_bucket_max_width).toInt(), 0
        )
    }

    override fun getItemViewType(position: Int): Int = viewHolderId
}