package ir.mehdiyari.fallery.buckets.bucketContent.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.databinding.FragmentPhotoPreviewBinding
import ir.mehdiyari.fallery.imageLoader.PhotoDiminution
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.models.Media
import ir.mehdiyari.fallery.utils.getHeightBasedOnScaledWidth

internal class PhotoPreviewFragment : AbstractMediaPreviewFragment() {

    private var _binding: FragmentPhotoPreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentPhotoPreviewBinding.inflate(inflater).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getParcelable<Media.Photo>("photo"))?.also { photo ->
            binding.photoView.setOnClickListener(onMediaPreviewClickListener)
            loadPhoto(photo)
        }
    }

    private fun loadPhoto(photo: Media.Photo) {
        val displayWidth = requireActivity().resources.displayMetrics.widthPixels
        val width = if (displayWidth >= photo.width) photo.width else displayWidth
        val height = if (width != photo.width) getHeightBasedOnScaledWidth(photo.width, photo.height, width) else photo.height
        FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideImageLoader().apply {
            if (photo.isGif())
                loadGif(requireContext(), binding.photoView, PhotoDiminution(width, height), R.color.fallery_black, photo.path)
            else
                loadPhoto(requireContext(), binding.photoView, PhotoDiminution(width, height), R.color.fallery_black, photo.path)
        }
    }

    override fun onDestroyView() {
        try {
            binding.frameLayoutPhotoPreviewRootLayout.setOnClickListener(null)
            onMediaPreviewClickListener = null
            _binding = null
        } catch (ignored: Throwable) {
            ignored.printStackTrace()
        }
        super.onDestroyView()
    }

}