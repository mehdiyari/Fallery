package ir.mehdiyari.fallery.repo

import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.models.MediaBucket

interface AbstractMediaBucketProvider {

    suspend fun getMediaBuckets(bucketType: BucketType): List<MediaBucket>

}