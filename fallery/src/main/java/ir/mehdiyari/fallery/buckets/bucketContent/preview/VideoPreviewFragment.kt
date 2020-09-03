package ir.mehdiyari.fallery.buckets.bucketContent.preview

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.imageLoader.PhotoDiminution
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.models.Media
import ir.mehdiyari.fallery.utils.*
import kotlinx.android.synthetic.main.fragment_video_preview.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class VideoPreviewFragment : AbstractMediaPreviewFragment(R.layout.fragment_video_preview) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getParcelable<Media.Video>("video"))?.also { video ->
            frameLayoutVideoPreviewRootLayout.setOnClickListener(onMediaPreviewClickListener)
            loadVideoThumbnail(video)
        }
    }

    private fun loadVideoThumbnail(video: Media.Video) {
        setupVideoToggleOnClickListener(video)
        MediaMetadataRetriever().autoClose { videoMetadataRetriever ->
            var videoOriginalSize = PhotoDiminution(0, 0)
            try {
                videoMetadataRetriever.setDataSource(video.path)
                videoOriginalSize = videoMetadataRetriever.getVideoSize()
            } catch (t: Throwable) {
                Log.e(FALLERY_LOG_TAG, "cant load video size from video path")
            }

            val displayMetrics = requireActivity().resources.displayMetrics
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    createThumbForVideosOrEmpty(
                        videosPath = listOf(video.path to video.id),
                        cacheDir = FalleryActivityComponentHolder.getOrNull()!!.provideCacheDir().cacheDir,
                        highQuality = true to PhotoDiminution(
                            if (videoOriginalSize.widthIsNotSet()) displayMetrics.widthPixels else videoOriginalSize.width,
                            if (videoOriginalSize.heightIsNotSet()) video.thumbnail.height else videoOriginalSize.height
                        )
                    ).also {
                        launch(Dispatchers.Main) {
                            if (it.isEmpty() || it.firstOrNull()?.isEmpty() == true)
                                showDefaultThumbnail(displayMetrics, video.thumbnail.path)
                            else
                                showVideoThumbnail(it, displayMetrics, videoOriginalSize)
                        }
                    }
                } catch (ignored: Throwable) {
                    showDefaultThumbnail(displayMetrics, video.thumbnail.path)
                }
            }
        }
    }

    private fun setupVideoToggleOnClickListener(video: Media.Video) {
        appCompatImageViewPlayVideo.setOnClickListener {
            FalleryActivityComponentHolder.getOrNull()?.provideFalleryOptions()?.onVideoPlayClick.also {
                if (it == null) {
                    Intent(Intent.ACTION_VIEW, Uri.parse(video.path)).apply {
                        setDataAndType(Uri.parse(video.path), "video/*")
                        if (this.resolveActivity(requireContext().packageManager!!) != null) {
                            try {
                                requireContext().startActivity(this)
                            } catch (t: ActivityNotFoundException) {
                                Toast.makeText(requireContext(), R.string.no_video_player_found, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    it.invoke(video.path)
                }
            }
        }
    }

    private fun showDefaultThumbnail(displayMetrics: DisplayMetrics, thumbnailPath: String) {
        FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideImageLoader()
            .loadPhoto(
                requireContext(),
                appCompatImageViewVideoThumbnail,
                PhotoDiminution(displayMetrics.widthPixels, displayMetrics.widthPixels / 2),
                R.color.fallery_black,
                thumbnailPath
            )
    }

    private fun showVideoThumbnail(
        thumbList: List<String>,
        displayMetrics: DisplayMetrics,
        videoOriginalSize: PhotoDiminution
    ) {
        FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideImageLoader()
            .loadPhoto(
                context = requireContext(),
                imageView = appCompatImageViewVideoThumbnail,
                resizeDiminution = PhotoDiminution(
                    width = displayMetrics.widthPixels,
                    height = if (videoOriginalSize.heightIsNotSet()) displayMetrics.heightPixels / 2 else getHeightBasedOnScaledWidth(
                        originalWidth = videoOriginalSize.width,
                        originalHeight = videoOriginalSize.height,
                        scaledWidth = displayMetrics.widthPixels
                    )
                ),
                placeHolderColor = R.color.fallery_black,
                path = thumbList.first()
            )
    }

    override fun onDestroyView() {
        try {
            frameLayoutVideoPreviewRootLayout.setOnClickListener(null)
            appCompatImageViewPlayVideo.setOnClickListener(null)
            onMediaPreviewClickListener = null
        } catch (ignored: Throwable) {
            ignored.printStackTrace()
        }
        super.onDestroyView()
    }
}
