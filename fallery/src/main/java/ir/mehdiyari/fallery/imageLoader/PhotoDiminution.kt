package ir.mehdiyari.fallery.imageLoader

data class PhotoDiminution(val width: Int, val height: Int) {
    fun isNotSet(): Boolean = width == 0 && height == 0
}