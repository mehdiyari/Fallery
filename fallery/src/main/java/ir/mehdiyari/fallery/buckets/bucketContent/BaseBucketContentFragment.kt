package ir.mehdiyari.fallery.buckets.bucketContent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.buckets.bucketContent.content.BucketContentFragment
import ir.mehdiyari.fallery.buckets.bucketContent.preview.PreviewFragment
import ir.mehdiyari.fallery.databinding.FragmentBaseBucketContentBinding
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder

internal class BaseBucketContentFragment : Fragment() {

    private var _binding: FragmentBaseBucketContentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentBaseBucketContentBinding.inflate(inflater).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        if (savedInstanceState == null) {
            arguments?.getLong("bucket_id")?.also { bucketId ->
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.layoutBucketContentFragmentContainer,
                        BucketContentFragment().apply {
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
            FalleryActivityComponentHolder.createOrGetComponent(requireActivity())
                .provideBucketContentViewModelFactory()
        )[BucketContentViewModel::class.java].apply {
            showPreviewFragmentLiveData.observe(viewLifecycleOwner) {
                if (it != null) {
                    navigateToPreviewFragment(fromMediaPath = it)
                }
            }
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}