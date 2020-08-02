package ir.mehdiyari.fallery.buckets.bucketList

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.main.fallery.BucketRecyclerViewItemMode
import ir.mehdiyari.fallery.main.ui.FalleryViewModel
import ir.mehdiyari.fallery.main.ui.MediaObserverInterface
import ir.mehdiyari.fallery.utils.*
import kotlinx.android.synthetic.main.fragment_bucket_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
internal class BucketListFragment : Fragment() {

    private lateinit var bucketListViewModel: BucketListViewModel
    private lateinit var falleryViewModel: FalleryViewModel

    private val bucketAdapter by lazy { FalleryActivityComponentHolder.getOrNull()!!.provideBucketListAdapter() }

    private lateinit var gridLayoutManager: GridLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_bucket_list, container, false)

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
                        val displayMetric = DisplayMetrics()
                        requireActivity().windowManager.defaultDisplay.getRealMetrics(displayMetric)
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
            bucketRecycleViewModeLiveData.observe(viewLifecycleOwner, Observer {
                changeRecyclerViewItemModeTo(it)
            })
        }

        bucketListViewModel = ViewModelProvider(
            this,
            FalleryActivityComponentHolder.componentCreator(requireActivity()).provideBucketListViewModelFactory()
        )[BucketListViewModel::class.java]

        lifecycleScope.launch {
            launch {
                falleryViewModel.storagePermissionGrantedStateFlow.collect {
                    if (it == true) {
                        bucketListViewModel.getBuckets()
                    }
                }
            }
        }

        if (FalleryActivityComponentHolder.getOrNull()?.provideFalleryOptions()?.mediaObserverEnabled == true) {
            (requireActivity() as MediaObserverInterface).getMediaObserverInstance()?.externalStorageChangeLiveData?.observe(viewLifecycleOwner, Observer {
                Log.d(FALLERY_LOG_TAG, "something changed in external Storage")
                requireActivity().permissionChecker(Manifest.permission.WRITE_EXTERNAL_STORAGE, granted = {
                    bucketListViewModel.getBuckets(true)
                }, denied = {
                    Log.e(FALLERY_LOG_TAG, "mediaStoreObserver -> requestBuckets -> app has not access to external storage for get buckets from mediaStore")
                })
            })
        }

        bucketListViewModel.apply {
            lifecycleScope.launch {
                loadingViewStateFlow.collect {
                    when (it) {
                        is LoadingViewState.Error -> showErrorLayout()
                        is LoadingViewState.ShowLoading -> showLoading()
                        is LoadingViewState.HideLoading -> hideLoading()
                    }
                }
            }

            lifecycleScope.launch {
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
        contentLoadingProgressBarBucketList.show()
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
        contentLoadingProgressBarBucketList.hide()
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
}