package ir.mehdiyari.fallery.buckets.bucketContent.preview

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.TranslateAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import ir.mehdiyari.fallery.buckets.bucketContent.BucketContentViewModel
import ir.mehdiyari.fallery.buckets.bucketContent.preview.adapter.MediaPreviewAdapter
import ir.mehdiyari.fallery.databinding.FragmentPreviewBinding
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.main.ui.FalleryToolbarVisibilityController
import ir.mehdiyari.fallery.main.ui.FalleryViewModel
import ir.mehdiyari.fallery.main.ui.MediaCountModel
import ir.mehdiyari.fallery.utils.createMediaCountSpannable
import ir.mehdiyari.fallery.utils.setOnAnimationEndListener
import kotlinx.coroutines.launch

internal class PreviewFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!
    

    private lateinit var bucketContentViewModel: BucketContentViewModel

    private lateinit var falleryViewModel: FalleryViewModel

    private val mediaPreviewAdapter by lazy {
        MediaPreviewAdapter(
            this@PreviewFragment,
            this
        )
    }
    private val selectedDrawable by lazy {
        FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideSelectedDrawable()
    }

    private val deselectDrawable by lazy {
        FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideDeselectedDrawable()
    }

    private val pageSelectedCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                showToolbarWithAnimation()
                checkForSelection(position)
                saveCurrentPosition(position)
            }
        }
    }

    private val falleryToolbarVisibilityController: FalleryToolbarVisibilityController by lazy {
        requireActivity() as FalleryToolbarVisibilityController
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun saveCurrentPosition(position: Int) {
        arguments?.putString(
            "from_media_path",
            bucketContentViewModel.getMediaPathByPosition(position)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentPreviewBinding.inflate(inflater).also { 
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        falleryToolbarVisibilityController.hideToolbar(false)
        initViewModel()
        initView()
    }

    override fun onStart() {
        super.onStart()
        binding.viewPagerMediaPreview.registerOnPageChangeCallback(pageSelectedCallback)
        if (binding.viewPagerMediaPreview.adapter == null)
            binding.viewPagerMediaPreview.adapter = mediaPreviewAdapter
    }

    private fun initView() {
        FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideFalleryOptions().apply {
            binding.viewPagerMediaPreview.orientation = mediaPreviewScrollOrientation
            binding.viewPagerMediaPreview.setPageTransformer(mediaPreviewPageTransformer)
        }

        binding.appCompatImageButtonMediaSelect.setOnClickListener {
            val position = binding.viewPagerMediaPreview.currentItem
            bucketContentViewModel.getMediaPathByPosition(position)?.also {
                if (falleryViewModel.isPhotoSelected(it))
                    falleryViewModel.requestDeselectingMedia(it)
                else
                    falleryViewModel.requestSelectingMedia(it)
            }

            checkForSelection(position)
        }

        binding.imageViewBackButton.setOnClickListener { requireActivity().onBackPressed() }
    }

    private fun checkForSelection(position: Int) {
        binding.appCompatImageButtonMediaSelect.setImageDrawable(bucketContentViewModel.getMediaPathByPosition(position).let {
            if (it != null && falleryViewModel.isPhotoSelected(it)) selectedDrawable else deselectDrawable
        })
    }

    private fun initViewModel() {
        falleryViewModel = ViewModelProvider(
            requireActivity(),
            FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideFalleryViewModelFactory()
        )[FalleryViewModel::class.java].apply {
            hideSendOrCaptionLayout()
            viewLifecycleOwner.lifecycleScope.launch {
                mediaCountStateFlow.collect { setupMediaCountView(it) }
            }
        }

        bucketContentViewModel = ViewModelProvider(
            requireParentFragment(),
            FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideBucketContentViewModelFactory()
        )[BucketContentViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            bucketContentViewModel.mediaList.collect {
                binding.viewPagerMediaPreview.adapter = mediaPreviewAdapter
                mediaPreviewAdapter.medias = it
                arguments?.getString("from_media_path", null).also { path ->
                    if (path != null) {
                        binding.viewPagerMediaPreview.post {
                            binding.viewPagerMediaPreview.setCurrentItem(bucketContentViewModel.getIndexOfPath(path).apply(this@PreviewFragment::checkForSelection), false)
                        }
                    }
                }
            }
        }
    }

    private fun setupMediaCountView(mediaCountModel: MediaCountModel) {
        if (mediaCountModel.selectedCount != 0 && mediaCountModel.totalCount != 0) {
            binding.textViewMediaPreviewTitle.text = createMediaCountSpannable(
                context = requireContext(),
                value = mediaCountModel,
                colorAccent = FalleryActivityComponentHolder.getOrNull()?.provideFalleryStyleAttrs()?.falleryColorAccent ?: Color.BLUE
            )
        } else {
            binding.textViewMediaPreviewTitle.text = ""
        }
    }

    private fun hideToolbarWithAnimation() {
        if (binding.toolbarMediaPreview.visibility == View.GONE) return
        binding.toolbarMediaPreview.startAnimation(TranslateAnimation(0f, 0f, 0f, -binding.toolbarMediaPreview.height.toFloat()).apply {
            duration = 200
            fillAfter = true
            setOnAnimationEndListener {
                binding.toolbarMediaPreview.visibility = View.GONE
                binding.toolbarMediaPreview.animation = null
            }
        })
    }

    private fun showToolbarWithAnimation() {
        if (binding.toolbarMediaPreview.visibility == View.VISIBLE) return
        binding.toolbarMediaPreview.visibility = View.INVISIBLE
        binding.toolbarMediaPreview.post {
            binding.toolbarMediaPreview.startAnimation(TranslateAnimation(0f, 0f, -binding.toolbarMediaPreview.height.toFloat(), 0f).apply {
                duration = 200
                fillAfter = true
                setOnAnimationEndListener {
                    binding.toolbarMediaPreview.visibility = View.VISIBLE
                    binding.toolbarMediaPreview.animation = null
                }
            })
        }
    }


    override fun onStop() {
        binding.viewPagerMediaPreview.unregisterOnPageChangeCallback(pageSelectedCallback)
        super.onStop()
    }

    override fun onDestroyView() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        falleryViewModel.showSendOrCaptionLayout()
        falleryToolbarVisibilityController.showToolbar(false)
        binding.viewPagerMediaPreview.adapter = null
        _binding = null
        super.onDestroyView()
    }

    // onViewPager Click
    override fun onClick(v: View?) {
        if (binding.toolbarMediaPreview.visibility == View.GONE)
            showToolbarWithAnimation()
        else
            hideToolbarWithAnimation()
    }
}