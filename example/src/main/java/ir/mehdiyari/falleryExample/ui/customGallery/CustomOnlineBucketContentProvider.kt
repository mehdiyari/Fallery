package ir.mehdiyari.falleryExample.ui.customGallery

import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.models.Media
import ir.mehdiyari.fallery.repo.AbstractBucketContentProvider
import ir.mehdiyari.falleryExample.utils.FalleryExample
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CustomOnlineBucketContentProvider : AbstractBucketContentProvider {

    override suspend fun getMediasOfBucket(bucketId: Long, bucketType: BucketType): Flow<List<Media>> = flow {
        emit(FalleryExample.customGalleryApiService!!.getBucketsContentById("bucket_$bucketId.json"))
    }

}