package ir.mehdiyari.fallery.buckets.ui.bucketList.adapter

import androidx.recyclerview.widget.DiffUtil
import ir.mehdiyari.fallery.models.MediaBucket

internal class MediaBucketDiffCallback : DiffUtil.ItemCallback<MediaBucket>() {

    override fun areItemsTheSame(oldItem: MediaBucket, newItem: MediaBucket): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: MediaBucket, newItem: MediaBucket): Boolean =
        oldItem == newItem
}