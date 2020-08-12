package ir.mehdiyari.fallery.buckets.bucketContent.content

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.buckets.bucketContent.BucketContentViewModel
import ir.mehdiyari.fallery.buckets.bucketList.LoadingViewState
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.main.ui.FalleryViewModel
import ir.mehdiyari.fallery.main.ui.MediaObserverInterface
import ir.mehdiyari.fallery.utils.FALLERY_LOG_TAG
import ir.mehdiyari.fallery.utils.divideScreenToEqualPart
import ir.mehdiyari.fallery.utils.dpToPx
import ir.mehdiyari.fallery.utils.permissionChecker
import kotlinx.android.synthetic.main.fragment_bucket_content.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BucketContentFragment : Fragment() {

    private lateinit var bucketContentViewModel: BucketContentViewModel
    private lateinit var falleryViewModel: FalleryViewModel

    private val bucketContentAdapter by lazy {
        FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideBucketContentAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_bucket_content, container, false)

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

        if (FalleryActivityComponentHolder.getOrNull()?.provideFalleryOptions()?.mediaObserverEnabled == true) {
            (requireActivity() as MediaObserverInterface).getMediaObserverInstance()?.externalStorageChangeLiveData?.observe(viewLifecycleOwner, Observer {
                if (!FalleryActivityComponentHolder.getOrNull()!!.provideFalleryOptions().grantExternalStoragePermission) {
                    bucketContentViewModel.getMedias(arguments?.getLong("bucket_id")!!, true)
                } else {
                    requireActivity().permissionChecker(Manifest.permission.WRITE_EXTERNAL_STORAGE, granted = {
                        Log.d(FALLERY_LOG_TAG, "mediaStoreOnChanged -> refresh medias in bucket")
                        bucketContentViewModel.getMedias(arguments?.getLong("bucket_id")!!, true)
                    }, denied = {
                        Log.e(FALLERY_LOG_TAG, "mediaStoreObserver -> getMedias -> app has not access to external storage for get medias of bucket from mediaStore")
                    })
                }
            })
        }

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
            bucketContentViewModel.retry(arguments!!.getLong("bucket_id"))
        }
    }

    private fun showLoading() {
        errorLayoutBucketContent.hide()
        recyclerViewBucketContent.visibility = View.GONE
        contentLoadingProgressBarBucketContent.show()
    }

    private fun hideLoading() {
        errorLayoutBucketContent.hide()
        recyclerViewBucketContent.visibility = View.VISIBLE
        contentLoadingProgressBarBucketContent.hide()
    }
}