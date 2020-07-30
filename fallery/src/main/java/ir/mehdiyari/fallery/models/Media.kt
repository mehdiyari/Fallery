package ir.mehdiyari.fallery.models

import android.os.Parcel
import android.os.Parcelable
import ir.mehdiyari.fallery.utils.getFileExtensionFromPath
import java.io.Serializable

sealed class Media {
    fun getMediaId(): Long = when (this) {
        is Photo -> id
        is Video -> id
    }

    fun getMediaPath(): String = when (this) {
        is Photo -> path
        is Video -> path
    }

    data class Photo(
        val id: Long,
        val path: String,
        val width: Int,
        val height: Int
    ) : Media(), Parcelable {

        fun isGif(): Boolean = getFileExtensionFromPath(path) == "gif"

        constructor(parcel: Parcel) : this(parcel.readLong(), parcel.readString() ?: "", parcel.readInt(), parcel.readInt())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(id)
            parcel.writeString(path)
            parcel.writeInt(width)
            parcel.writeInt(height)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Photo> {
            fun empty() = Photo(0, "", 0, 0)
            override fun createFromParcel(parcel: Parcel): Photo = Photo(parcel)
            override fun newArray(size: Int): Array<Photo?> = arrayOfNulls(size)
        }
    }

    data class Video(
        val id: Long,
        val path: String,
        val duration: Long,
        val thumbnail: Photo
    ) : Media(), Serializable, Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readLong(),
            parcel.readParcelable(Photo::class.java.classLoader)!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(id)
            parcel.writeString(path)
            parcel.writeLong(duration)
            parcel.writeParcelable(thumbnail, flags)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Video> {
            override fun createFromParcel(parcel: Parcel): Video = Video(parcel)
            override fun newArray(size: Int): Array<Video?> = arrayOfNulls(size)
        }
    }
}