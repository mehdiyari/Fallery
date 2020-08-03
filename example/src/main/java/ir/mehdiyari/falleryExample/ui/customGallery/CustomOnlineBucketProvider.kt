package ir.mehdiyari.falleryExample.ui.customGallery

import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.models.MediaBucket
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider
import ir.mehdiyari.falleryExample.utils.FalleryExample

class CustomOnlineBucketProvider : AbstractMediaBucketProvider {

    override suspend fun getMediaBuckets(bucketType: BucketType): List<MediaBucket> = FalleryExample.customGalleryApiService!!.getBucketList().map {
        MediaBucket(it.id, it.thumbnail, it.displayName, it.thumbnail, it.mediaCount)
    }

}