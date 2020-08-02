package ir.mehdiyari.fallery.main.ui

internal sealed class FalleryView {
    object BucketList : FalleryView()
    data class BucketContent(val bucketId: Long, val bucketName: String) : FalleryView()
}