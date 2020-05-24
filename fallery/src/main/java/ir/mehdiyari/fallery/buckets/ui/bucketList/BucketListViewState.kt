package ir.mehdiyari.fallery.buckets.ui.bucketList

internal sealed class BucketListViewState {
    object ShowLoading : BucketListViewState()
    object HideLoading : BucketListViewState()
    object ErrorInFetchingBuckets : BucketListViewState()
}