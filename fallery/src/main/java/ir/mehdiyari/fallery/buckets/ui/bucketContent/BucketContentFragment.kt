package ir.mehdiyari.fallery.buckets.ui.bucketContent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.main.ui.FalleryViewModel
import ir.mehdiyari.fallery.utils.divideScreenToEqualPart
import ir.mehdiyari.fallery.utils.dpToPx
import kotlinx.android.synthetic.main.fragment_bucket_content.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
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
            getItemViewWidth = {
                (resources.displayMetrics.widthPixels / bucketContentLayoutManager.spanCount) - dpToPx(2)
            }
        }
    }

    private fun initViewModel() {
        falleryViewModel = ViewModelProvider(
            requireActivity(),
            FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideFalleryViewModelFactory()
        )[FalleryViewModel::class.java]
        bucketContentViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).let {
                @Suppress("UNCHECKED_CAST")
                BucketContentViewModel(
                    it.provideBucketContentProvider(), it.provideFalleryOptions().mediaTypeFilterOptions.bucketType
                ) as T
            }
        })[BucketContentViewModel::class.java]

        arguments?.getLong("bucket_id")?.also {
            bucketContentViewModel.getMedias(it)
        } ?: requireActivity().onBackPressed()

        lifecycleScope.launch {
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

}