package ir.mehdiyari.fallery.repo

import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.models.Media
import kotlinx.coroutines.flow.Flow

interface AbstractBucketContentProvider {

    /**
     * get medias in bucket with [bucketId] based on [bucketType]
     * @param bucketType BucketType filtering type
     * @param bucketId Long id of bucket
     * @return Flow<List<Media>> return flow of List<Media>
     */
    suspend fun getMediasOfBucket(
        bucketId: Long,
        bucketType: BucketType
    ): Flow<List<Media>>

}