package ir.mehdiyari.falleryExample.utils

import com.squareup.moshi.Json

data class NetBucketModel(
    @field:Json(name = "id") val id: Long,
    @field:Json(name = "display_name") val displayName: String,
    @field:Json(name = "thumbnail_url") val thumbnail: String,
    @field:Json(name = "media_count") val mediaCount: Int
) {
    constructor() : this(0, "", "", 0)
}
