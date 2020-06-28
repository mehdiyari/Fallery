package ir.mehdiyari.fallery.main.ui

internal sealed class FalleryView {
    object BucketList : FalleryView()
    data class BucketContent(val bucketId: Long) : FalleryView()
    data class PhotoPreview(val bucketId: Long, val mediaIndex: Int) : FalleryView()
}