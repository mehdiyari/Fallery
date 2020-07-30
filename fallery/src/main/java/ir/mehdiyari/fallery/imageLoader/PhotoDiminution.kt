package ir.mehdiyari.fallery.imageLoader

data class PhotoDiminution(val width: Int, val height: Int) {
    fun isNotSet(): Boolean = widthIsNotSet() && heightIsNotSet()

    fun widthIsNotSet() = width == 0
    fun heightIsNotSet() = height == 0
}