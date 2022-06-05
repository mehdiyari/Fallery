package ir.mehdiyari.fallery.buckets.bucketList

import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.main.fallery.BucketRecyclerViewItemMode
import ir.mehdiyari.fallery.main.ui.FalleryViewModel
import ir.mehdiyari.fallery.utils.divideScreenToEqualPart
import ir.mehdiyari.fallery.utils.dpToPx
import ir.mehdiyari.fallery.utils.setOnAnimationEndListener
import kotlinx.android.synthetic.main.fragment_bucket_list.*
import kotlinx.coroutines.launch

internal class BucketListFragment : Fragment(R.layout.fragment_bucket_list) {

    private lateinit var bucketListViewModel: BucketListViewModel
    private lateinit var falleryViewModel: FalleryViewModel

    private val bucketAdapter by lazy { FalleryActivityComponentHolder.getOrNull()!!.provideBucketListAdapter() }
    private lateinit var gridLayoutManager: GridLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gridLayoutManager = GridLayoutManager(requireContext(), getSpanCountBasedOnRecyclerViewMode())
        initViewModel()
        initView()
    }

    private fun initView() {
        bucketAdapter.viewHolderId = FalleryActivityComponentHolder
            .getOrNull()?.provideFalleryOptions()?.bucketRecyclerViewItemMode?.value ?: R.layout.grid_bucket_item_view

        recyclerViewBuckets.apply {
            adapter = bucketAdapter
            layoutManager = gridLayoutManager
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        bucketAdapter.apply {
            onBucketClick = {
                falleryViewModel.openBucketWithId(it, bucketListViewModel.getBucketNameById(it))
            }

            getImageViewWidth = {
                if (recyclerViewBuckets.layoutManager is GridLayoutManager) {
                    (recyclerViewBuckets.layoutManager as GridLayoutManager).let { gridLayoutManager ->
                        val displayMetric = requireActivity().resources.displayMetrics
                        (((displayMetric.widthPixels - dpToPx(3) * (gridLayoutManager.spanCount - 1)) / gridLayoutManager.spanCount) * 0.5).toInt()
                    }
                } else {
                    dpToPx(70)
                }
            }
        }
    }

    private fun initViewModel() {
        falleryViewModel = ViewModelProvider(
            requireActivity(),
            FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideFalleryViewModelFactory()
        )[FalleryViewModel::class.java].apply {
            bucketRecycleViewModeLiveData.observe(viewLifecycleOwner) {
                changeRecyclerViewItemModeTo(it)
            }
        }

        bucketListViewModel = ViewModelProvider(
            this,
            FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideBucketListViewModelFactory()
        )[BucketListViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                falleryViewModel.storagePermissionGrantedStateFlow.collect {
                    if (it == true) {
                        bucketListViewModel.getBuckets()
                    }
                }
            }
        }

        bucketListViewModel.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                loadingViewStateFlow.collect {
                    when (it) {
                        is LoadingViewState.Error -> showErrorLayout()
                        is LoadingViewState.ShowLoading -> showLoading()
                        is LoadingViewState.HideLoading -> hideLoading()
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                launch { allMediaCountChanged.collect { falleryViewModel.totalMediaCount = it } }
                bucketsStateFlow.collect { bucketAdapter.submitList(it) }
            }
        }
    }

    private fun changeRecyclerViewItemModeTo(bucketRecyclerViewItemMode: BucketRecyclerViewItemMode) {
        if (recyclerViewBuckets.layoutManager is GridLayoutManager) {
            bucketAdapter.viewHolderId = bucketRecyclerViewItemMode.value
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) TransitionManager.beginDelayedTransition(recyclerViewBuckets)
            (recyclerViewBuckets.layoutManager as GridLayoutManager).spanCount = getSpanCountBasedOnRecyclerViewMode()
        }
    }


    private fun showErrorLayout() {
        hideLoading()
        recyclerViewBuckets.visibility = View.GONE
        errorLayoutBucketList.show()
        errorLayoutBucketList.setOnRetryClickListener { bucketListViewModel.retry() }
    }


    private fun showLoading() {
        errorLayoutBucketList.hide()
        contentLoadingProgressBarBucketList.visibility = View.VISIBLE
        if (recyclerViewBuckets.visibility == View.VISIBLE) {
            recyclerViewBuckets.startAnimation(
                AlphaAnimation(1f, 0.5f).apply {
                    duration = 250
                    setOnAnimationEndListener {
                        recyclerViewBuckets.visibility = View.GONE
                    }
                }
            )
        }
    }

    private fun hideLoading() {
        errorLayoutBucketList.hide()
        contentLoadingProgressBarBucketList.visibility = View.GONE
        recyclerViewBuckets.visibility = View.INVISIBLE
        recyclerViewBuckets.startAnimation(
            AlphaAnimation(0.5f, 1f).apply {
                duration = 250
                setOnAnimationEndListener {
                    recyclerViewBuckets.visibility = View.VISIBLE
                }
            }
        )
    }

    private fun getSpanCountBasedOnRecyclerViewMode(): Int = if (FalleryActivityComponentHolder.getOrNull()?.provideFalleryOptions()?.bucketRecyclerViewItemMode ==
        BucketRecyclerViewItemMode.GridStyle
    ) {
        divideScreenToEqualPart(
            displayWidth = resources.displayMetrics.widthPixels,
            itemWidth = resources.getDimension(R.dimen.fallery_bucket_max_width),
            minCount = 2
        )
    } else 1

    override fun onDestroyView() {
        recyclerViewBuckets?.adapter = null
        recyclerViewBuckets?.layoutManager = null
        super.onDestroyView()
    }
}