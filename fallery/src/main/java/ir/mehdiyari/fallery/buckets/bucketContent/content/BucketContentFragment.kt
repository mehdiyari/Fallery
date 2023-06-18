package ir.mehdiyari.fallery.buckets.bucketContent.content

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.buckets.bucketContent.BucketContentSpanCount
import ir.mehdiyari.fallery.buckets.bucketContent.BucketContentViewModel
import ir.mehdiyari.fallery.buckets.bucketList.LoadingViewState
import ir.mehdiyari.fallery.databinding.FragmentBucketContentBinding
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.main.fallery.FalleryBucketsSpanCountMode
import ir.mehdiyari.fallery.main.ui.FalleryViewModel
import ir.mehdiyari.fallery.utils.divideScreenToEqualPart
import ir.mehdiyari.fallery.utils.dpToPx
import kotlinx.coroutines.launch

internal class BucketContentFragment : Fragment() {

    private var _binding: FragmentBucketContentBinding? = null
    private val binding get() = _binding!!

    private lateinit var bucketContentViewModel: BucketContentViewModel
    private lateinit var falleryViewModel: FalleryViewModel
    private val bucketContentAdapter by lazy {
        FalleryActivityComponentHolder.createOrGetComponent(requireActivity())
            .provideBucketContentAdapter()
    }

    private var recyclerViewTouchListener: RecyclerViewTouchListener? = null
    private val onChangeSpanCountCallback by lazy<(Boolean) -> Unit> {
        {
            bucketContentViewModel.changeSpanCountBasedOnUserTouch(
                it,
                recyclerViewTouchListener!!.maxSpanCount.toInt(),
                recyclerViewTouchListener!!.minSpanCount.toInt(),
                bucketContentLayoutManager?.spanCount,
                resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            )
        }
    }
    private var bucketContentLayoutManager: GridLayoutManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentBucketContentBinding.inflate(inflater).also {
            _binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
    }

    private fun initView() {
        val validSpanCount =
            bucketContentViewModel.spanCountStateFlow.value?.let(this::getSpanCountBasedOnOrientation)
                ?: divideScreenToEqualPart(
                    resources.displayMetrics.widthPixels,
                    resources.getDimension(R.dimen.min_size_bucket_content_item),
                    4
                )

        bucketContentAdapter.spanCount = validSpanCount
        bucketContentLayoutManager = GridLayoutManager(
            requireContext(),
            validSpanCount
        )
        binding.recyclerViewBucketContent.apply {
            adapter = bucketContentAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            setHasFixedSize(true)
            layoutManager = bucketContentLayoutManager
        }

        if (FalleryActivityComponentHolder
                .getOrNull()
                ?.provideFalleryOptions()?.falleryBucketsSpanCountMode == FalleryBucketsSpanCountMode.UserZoomInOrZoomOut
        ) {
            initializeGridSpanCountBasedOnUserTouches()
        }

        with(bucketContentAdapter) {
            selectedMediaTracker = falleryViewModel.mediaSelectionTracker
            onMediaSelected = falleryViewModel::requestSelectingMedia
            onMediaDeselected = falleryViewModel::requestDeselectingMedia
            onMediaClick = bucketContentViewModel::showPreviewFragment
            getItemViewWidth = {
                (resources.displayMetrics.widthPixels / bucketContentLayoutManager!!.spanCount) - dpToPx(
                    2
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeGridSpanCountBasedOnUserTouches() {
        if (recyclerViewTouchListener == null) {
            recyclerViewTouchListener = RecyclerViewTouchListener(
                binding.recyclerViewBucketContent,
                requireContext(),
                onChangeSpanCountCallback
            )
        }
        binding.recyclerViewBucketContent.setOnTouchListener(recyclerViewTouchListener)

        viewLifecycleOwner.lifecycleScope.launch {
            bucketContentViewModel.spanCountStateFlow.collect {
                if (it != null) {
                    changeSpanCount(getSpanCountBasedOnOrientation(it))
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeSpanCount(spanCount: Int) {
        if (spanCount != bucketContentLayoutManager!!.spanCount) {
            bucketContentAdapter.spanCount = spanCount
            bucketContentLayoutManager?.spanCount = spanCount
            bucketContentAdapter.notifyDataSetChanged()
        }
    }

    private fun getSpanCountBasedOnOrientation(
        bucketContentSpanCount: BucketContentSpanCount
    ): Int =
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            bucketContentSpanCount.portraitSpanCount
        else
            bucketContentSpanCount.landScapeSpanCount


    @SuppressLint("NotifyDataSetChanged")
    private fun initViewModel() {
        falleryViewModel = ViewModelProvider(
            requireActivity(),
            FalleryActivityComponentHolder.createOrGetComponent(requireActivity())
                .provideFalleryViewModelFactory()
        )[FalleryViewModel::class.java]

        bucketContentViewModel = ViewModelProvider(
            requireParentFragment(),
            FalleryActivityComponentHolder.getOrNull()!!.provideBucketContentViewModelFactory()
        )[BucketContentViewModel::class.java].apply {
            viewLifecycleOwner.lifecycleScope.launch {
                loadingViewStateFlow.collect {
                    when (it) {
                        is LoadingViewState.ShowLoading -> showLoading()
                        is LoadingViewState.HideLoading -> hideLoading()
                        is LoadingViewState.Error -> showErrorLayout()
                        else -> {}
                    }
                }
            }
        }

        arguments?.getLong("bucket_id")?.also {
            bucketContentViewModel.getMedias(it)
        } ?: requireActivity().onBackPressed()

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                bucketContentViewModel.mediaList.collect {
                    bucketContentAdapter.submitList(it)
                }
            }

            falleryViewModel.allMediaDeselectedStateFlow.collect {
                if (it)
                    bucketContentAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showErrorLayout() {
        hideLoading()
        binding.recyclerViewBucketContent.visibility = View.GONE
        binding.errorLayoutBucketContent.show()
        binding.errorLayoutBucketContent.setOnRetryClickListener {
            bucketContentViewModel.retry(requireArguments().getLong("bucket_id"))
        }
    }

    private fun showLoading() {
        binding.errorLayoutBucketContent.hide()
        binding.recyclerViewBucketContent.visibility = View.GONE
        binding.contentLoadingProgressBarBucketContent.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.errorLayoutBucketContent.hide()
        binding.recyclerViewBucketContent.visibility = View.VISIBLE
        binding.contentLoadingProgressBarBucketContent.visibility = View.GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onDestroyView() {
        binding.recyclerViewBucketContent.setOnTouchListener(null)
        recyclerViewTouchListener = null
        binding.recyclerViewBucketContent.adapter = null
        binding.recyclerViewBucketContent.layoutManager = null
        bucketContentLayoutManager = null
        _binding = null
        super.onDestroyView()
    }
}