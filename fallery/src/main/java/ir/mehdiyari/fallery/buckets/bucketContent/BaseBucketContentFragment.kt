package ir.mehdiyari.fallery.buckets.bucketContent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.buckets.bucketContent.content.BucketContentFragment
import ir.mehdiyari.fallery.buckets.bucketContent.preview.PreviewFragment
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
internal class BaseBucketContentFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_base_bucket_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        if (savedInstanceState == null) {
            arguments?.getLong("bucket_id")?.also { bucketId ->
                childFragmentManager.beginTransaction()
                    .replace(R.id.layoutBucketContentFragmentContainer, BucketContentFragment().apply {
                        arguments = Bundle().apply {
                            putLong("bucket_id", bucketId)
                        }
                    })
                    .addToBackStack(null)
                    .commit()
            } ?: requireActivity().onBackPressed()
        }
    }

    private fun initViewModel() {
        ViewModelProvider(
            this,
            FalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideBucketContentViewModelFactory()
        )[BucketContentViewModel::class.java].apply {
            showPreviewFragmentLiveData.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    navigateToPreviewFragment(fromMediaPath = it)
                }
            })
        }
    }

    private fun navigateToPreviewFragment(fromMediaPath: String) {
        childFragmentManager.beginTransaction()
            .replace(R.id.layoutBucketContentFragmentContainer, PreviewFragment().apply {
                arguments = Bundle().apply {
                    putString("from_media_path", fromMediaPath)
                }
            })
            .addToBackStack(null)
            .commit()
    }

}