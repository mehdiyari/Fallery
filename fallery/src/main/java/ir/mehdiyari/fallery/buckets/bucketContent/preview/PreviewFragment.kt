package ir.mehdiyari.fallery.buckets.bucketContent.preview

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
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.buckets.bucketContent.BucketContentViewModel
import ir.mehdiyari.fallery.buckets.bucketContent.preview.adapter.MediaPreviewAdapter
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.main.ui.FalleryToolbarVisibilityController
import ir.mehdiyari.fallery.main.ui.FalleryViewModel
import ir.mehdiyari.fallery.main.ui.MediaCountModel
import ir.mehdiyari.fallery.utils.createMediaCountSpannable
import ir.mehdiyari.fallery.utils.setOnAnimationEndListener
import kotlinx.android.synthetic.main.fragment_preview.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class PreviewFragment : Fragment(), View.OnClickListener {

    private lateinit var bucketContentViewModel: BucketContentViewModel

    @ExperimentalCoroutinesApi
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

    @ExperimentalCoroutinesApi
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_preview, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        falleryToolbarVisibilityController.hideToolbar(false)
    }

    private fun saveCurrentPosition(position: Int) {
        arguments?.putString(
            "from_media_path",
            bucketContentViewModel.getMediaPathByPosition(position)
        )
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
    }

    @ExperimentalCoroutinesApi
    override fun onStart() {
        super.onStart()
        viewPagerMediaPreview.registerOnPageChangeCallback(pageSelectedCallback)
        if (viewPagerMediaPreview.adapter == null)
            viewPagerMediaPreview.adapter = mediaPreviewAdapter
    }

    @ExperimentalCoroutinesApi
    private fun initView() {
        FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideFalleryOptions().apply {
            viewPagerMediaPreview.orientation = mediaPreviewScrollOrientation
            viewPagerMediaPreview.setPageTransformer(mediaPreviewPageTransformer)
        }

        appCompatImageButtonMediaSelect.setOnClickListener {
            val position = viewPagerMediaPreview.currentItem
            bucketContentViewModel.getMediaPathByPosition(position)?.also {
                if (falleryViewModel.isPhotoSelected(it))
                    falleryViewModel.requestDeselectingMedia(it)
                else
                    falleryViewModel.requestSelectingMedia(it)
            }

            checkForSelection(position)
        }

        imageViewBackButton.setOnClickListener { requireActivity().onBackPressed() }
    }

    @ExperimentalCoroutinesApi
    private fun checkForSelection(position: Int) {
        appCompatImageButtonMediaSelect.background = bucketContentViewModel.getMediaPathByPosition(position).let {
            if (it != null && falleryViewModel.isPhotoSelected(it)) selectedDrawable else deselectDrawable
        }
    }

    @ExperimentalCoroutinesApi
    private fun initViewModel() {
        falleryViewModel = ViewModelProvider(
            requireActivity(),
            FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideFalleryViewModelFactory()
        )[FalleryViewModel::class.java].apply {
            hideSendOrCaptionLayout()
            lifecycleScope.launch {
                mediaCountStateFlow.collect { setupMediaCountView(it) }
            }
        }

        bucketContentViewModel = ViewModelProvider(
            requireParentFragment(),
            FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideBucketContentViewModelFactory()
        )[BucketContentViewModel::class.java]

        lifecycleScope.launch {
            bucketContentViewModel.mediaList.collect {
                if (viewPagerMediaPreview == null)
                    viewPagerMediaPreview.adapter = mediaPreviewAdapter

                mediaPreviewAdapter.medias = it
                arguments?.getString("from_media_path", null).also { path ->
                    if (path != null) {
                        viewPagerMediaPreview.post {
                            viewPagerMediaPreview.setCurrentItem(bucketContentViewModel.getIndexOfPath(path).apply(this@PreviewFragment::checkForSelection), false)
                        }
                    }
                }
            }
        }
    }

    private fun setupMediaCountView(mediaCountModel: MediaCountModel) {
        if (mediaCountModel.selectedCount != 0 && mediaCountModel.totalCount != 0) {
            textViewMediaPreviewTitle.text = createMediaCountSpannable(
                context = requireContext(),
                value = mediaCountModel,
                colorAccent = FalleryActivityComponentHolder.getOrNull()?.provideFalleryStyleAttrs()?.falleryColorAccent ?: Color.BLUE
            )
        } else {
            textViewMediaPreviewTitle.text = ""
        }
    }

    private fun hideToolbarWithAnimation() {
        if (toolbarMediaPreview.visibility == View.GONE) return
        toolbarMediaPreview.startAnimation(TranslateAnimation(0f, 0f, 0f, -toolbarMediaPreview.height.toFloat()).apply {
            duration = 200
            fillAfter = true
            setOnAnimationEndListener {
                toolbarMediaPreview.visibility = View.GONE
                toolbarMediaPreview.animation = null
            }
        })
    }

    private fun showToolbarWithAnimation() {
        if (toolbarMediaPreview.visibility == View.VISIBLE) return
        toolbarMediaPreview.visibility = View.INVISIBLE
        toolbarMediaPreview.post {
            toolbarMediaPreview.startAnimation(TranslateAnimation(0f, 0f, -toolbarMediaPreview.height.toFloat(), 0f).apply {
                duration = 200
                fillAfter = true
                setOnAnimationEndListener {
                    toolbarMediaPreview.visibility = View.VISIBLE
                    toolbarMediaPreview.animation = null
                }
            })
        }
    }


    @ExperimentalCoroutinesApi
    override fun onStop() {
        viewPagerMediaPreview.unregisterOnPageChangeCallback(pageSelectedCallback)
        viewPagerMediaPreview.adapter = null
        super.onStop()
    }

    @ExperimentalCoroutinesApi
    override fun onDestroyView() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        falleryViewModel.showSendOrCaptionLayout()
        falleryToolbarVisibilityController.showToolbar(false)
        super.onDestroyView()
    }


    // onViewPager Click
    override fun onClick(v: View?) {
        if (toolbarMediaPreview.visibility == View.GONE)
            showToolbarWithAnimation()
        else
            hideToolbarWithAnimation()
    }
}