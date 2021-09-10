package ir.mehdiyari.fallery.buckets.bucketContent.content

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.buckets.bucketContent.BucketContentViewModel
import ir.mehdiyari.fallery.buckets.bucketList.LoadingViewState
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.main.ui.FalleryViewModel
import ir.mehdiyari.fallery.utils.divideScreenToEqualPart
import ir.mehdiyari.fallery.utils.dpToPx
import kotlinx.android.synthetic.main.fragment_bucket_content.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
internal class BucketContentFragment : Fragment(R.layout.fragment_bucket_content) {

    private lateinit var bucketContentViewModel: BucketContentViewModel
    private lateinit var falleryViewModel: FalleryViewModel

    private val bucketContentAdapter by lazy {
        FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideBucketContentAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
    }

    private fun initView() {
        val bucketContentLayoutManager = GridLayoutManager(
            requireContext(),
            divideScreenToEqualPart(resources.displayMetrics.widthPixels, resources.getDimension(R.dimen.min_size_bucket_content_item), 4)
        )
        recyclerViewBucketContent.apply {
            adapter = bucketContentAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            setHasFixedSize(true)
            layoutManager = bucketContentLayoutManager
        }

        with(bucketContentAdapter) {
            selectedMediaTracker = falleryViewModel.mediaSelectionTracker
            onMediaSelected = falleryViewModel::requestSelectingMedia
            onMediaDeselected = falleryViewModel::requestDeselectingMedia
            onMediaClick = bucketContentViewModel::showPreviewFragment
            getItemViewWidth = { (resources.displayMetrics.widthPixels / bucketContentLayoutManager.spanCount) - dpToPx(2) }
        }
    }

    private fun initViewModel() {
        falleryViewModel = ViewModelProvider(
            requireActivity(),
            FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideFalleryViewModelFactory()
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
        recyclerViewBucketContent.visibility = View.GONE
        errorLayoutBucketContent.show()
        errorLayoutBucketContent.setOnRetryClickListener {
            bucketContentViewModel.retry(requireArguments().getLong("bucket_id"))
        }
    }

    private fun showLoading() {
        errorLayoutBucketContent.hide()
        recyclerViewBucketContent.visibility = View.GONE
        contentLoadingProgressBarBucketContent.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        errorLayoutBucketContent.hide()
        recyclerViewBucketContent.visibility = View.VISIBLE
        contentLoadingProgressBarBucketContent.visibility = View.GONE
    }

    override fun onDestroyView() {
        recyclerViewBucketContent?.adapter = null
        recyclerViewBucketContent?.layoutManager = null
        super.onDestroyView()
    }
}