package ir.mehdiyari.fallery.repo

import ir.mehdiyari.fallery.models.MediaBucket
import ir.mehdiyari.fallery.models.BucketType

interface AbstractMediaBucketProvider {

    suspend fun getMediaBuckets(bucketType: BucketType): List<MediaBucket>

}