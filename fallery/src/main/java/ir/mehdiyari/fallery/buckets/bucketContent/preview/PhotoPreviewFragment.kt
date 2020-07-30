package ir.mehdiyari.fallery.buckets.bucketContent.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.imageLoader.PhotoDiminution
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.models.Media
import ir.mehdiyari.fallery.utils.getHeightBasedOnScaledWidth
import kotlinx.android.synthetic.main.fragment_photo_preview.*

internal class PhotoPreviewFragment : AbstractMediaPreviewFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_photo_preview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getParcelable<Media.Photo>("photo"))?.also { photo ->
            photoView.setOnClickListener(onMediaPreviewClickListener)
            loadPhoto(photo)
        }
    }

    private fun loadPhoto(photo: Media.Photo) {
        val displayWidth = requireActivity().resources.displayMetrics.widthPixels
        val width = if (displayWidth >= photo.width) photo.width else displayWidth
        val height = if (width != photo.width) getHeightBasedOnScaledWidth(photo.width, photo.height, width) else photo.height
        FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideImageLoader().apply {
            if (photo.isGif())
                loadGif(requireContext(), photoView, PhotoDiminution(width, height), photo.path)
            else
                loadPhoto(requireContext(), photoView, PhotoDiminution(width, height), photo.path)
        }
    }

    override fun onDestroyView() {
        try {
            frameLayoutPhotoPreviewRootLayout.setOnClickListener(null)
            onMediaPreviewClickListener = null
        } catch (ignored: Throwable) {
            ignored.printStackTrace()
        }
        super.onDestroyView()
    }

}