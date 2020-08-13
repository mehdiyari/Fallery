package ir.mehdiyari.fallery.utils

internal enum class VideoMediaTypes constructor(override var value: Pair<String, List<String>>) :
    EnumType<Pair<String, List<String>>> {

    MP4(
        "video/mp4" to listOf(
            "mp4",
            "m4v"
        )
    ),

    QUICKTIME(
        "video/quicktime" to listOf(
            "mov"
        )
    ),

    THREEGPP(
        "video/3gpp" to listOf(
            "3gp",
            "3gpp"
        )
    ),

    THREEGPP2(
        "video/3gpp2" to listOf(
            "3g2",
            "3gpp2"
        )
    ),

    MKV(
        "video/x-matroska" to listOf(
            "mkv"
        )
    ),

    WEBM(
        "video/webm" to listOf(
            "webm"
        )
    ),

    TS(
        "video/mp2ts" to listOf(
            "ts"
        )
    ),

    AVI(
        "video/avi" to listOf(
            "avi"
        )
    );
}