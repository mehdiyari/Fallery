package ir.mehdiyari.falleryExample.ui

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import ir.mehdiyari.fallery.main.fallery.BucketRecyclerViewItemMode
import ir.mehdiyari.fallery.main.fallery.CameraEnabledOptions
import ir.mehdiyari.fallery.main.fallery.CaptionEnabledOptions
import ir.mehdiyari.fallery.main.fallery.FalleryBucketsSpanCountMode
import ir.mehdiyari.fallery.main.fallery.FalleryBuilder
import ir.mehdiyari.fallery.main.fallery.FalleryOptions
import ir.mehdiyari.fallery.main.fallery.registerFalleryResultCallback
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.falleryExample.R
import ir.mehdiyari.falleryExample.databinding.ActivityMainBinding
import ir.mehdiyari.falleryExample.ui.customGallery.CustomOnlineBucketContentProvider
import ir.mehdiyari.falleryExample.ui.customGallery.CustomOnlineBucketProvider
import ir.mehdiyari.falleryExample.utils.FalleryExample
import ir.mehdiyari.falleryExample.utils.GlideImageLoader

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val fileProviderAuthority by lazy { "${application.packageName}.provider" }
    private var itemIdSelected: Int = R.id.menuDefaultOptions
    private val mediaAdapter by lazy { MediaAdapter() }
    private var listCurrentMedias = listOf<Pair<String, String>>()
    private val glideImageLoader by lazy { GlideImageLoader() }
    private val falleryActivityResult =
        registerFalleryResultCallback(onResult = { result ->
            handleFalleryResult(result.mediaPathList, result.caption)
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(layoutInflater).also {
            _binding = it
            setContentView(it.root)
        }
        if (savedInstanceState != null) {
            itemIdSelected = savedInstanceState.getInt("itemIdSelected")
            try {
                listCurrentMedias = savedInstanceState.getStringArray("results")?.map { item ->
                    item.split("~|~").let { it[0] to it[1] }
                }?.toList()!!
            } catch (ignored: Throwable) {
                ignored.printStackTrace()
            }
        }
        initView()
    }

    private fun initView() {
        binding.bottomAppBarExample.setOnMenuItemClickListener {
            if (it.itemId == R.id.menuClear) {
                listCurrentMedias = listOf()
                mediaAdapter.submitList(listCurrentMedias)
                visibleRecyclerViewIfCurrentListIsNotEmpty()
            }

            true
        }

        binding.bottomAppBarExample.setNavigationOnClickListener {
            if (supportFragmentManager.getFragment(Bundle(), "bndf") == null) {
                val bottomNavigationDrawerFragment = BottomNavigationDrawerFragment()
                bottomNavigationDrawerFragment.onMenuItemSelected = { itemSelected ->
                    itemIdSelected = itemSelected
                    animateLayouts()
                }
                if (!bottomNavigationDrawerFragment.isAdded) {
                    bottomNavigationDrawerFragment.selectedItemId = itemIdSelected
                    bottomNavigationDrawerFragment.show(supportFragmentManager, "bndf")
                }
            }
        }

        binding.fabOpenFallery.setOnClickListener {
            openFalleryBasedOnSelectedMode()
        }


        visibleRecyclerViewIfCurrentListIsNotEmpty()
        mediaAdapter.submitList(listCurrentMedias)
        binding.recyclerViewResults.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = mediaAdapter
        }
    }

    private fun visibleRecyclerViewIfCurrentListIsNotEmpty() {
        if (listCurrentMedias.isEmpty()) {
            binding.textViewNotSelectedMediaMessage.visibility = View.VISIBLE
            binding.recyclerViewResults.visibility = View.GONE
        } else {
            binding.textViewNotSelectedMediaMessage.visibility = View.GONE
            binding.recyclerViewResults.visibility = View.VISIBLE
        }
    }

    private fun openFalleryBasedOnSelectedMode() {
        (when (itemIdSelected) {
            R.id.menuDefaultOptions -> FalleryOptions(glideImageLoader)
            R.id.menuMediaObserverEnabled -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader).setMediaObserverEnabled(true)
                    .build()
            }

            R.id.menuCameraEnabled -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setCameraEnabledOptions(
                        CameraEnabledOptions(
                            enabled = true,
                            fileProviderAuthority = fileProviderAuthority
                        )
                    ).build()
            }

            R.id.menuFilterTypePhoto -> {
                FalleryBuilder()
                    .mediaTypeFiltering(BucketType.ONLY_PHOTO_BUCKETS)
                    .setImageLoader(glideImageLoader)
                    .build()
            }

            R.id.menuFilterTypeVideo -> {
                FalleryBuilder()
                    .mediaTypeFiltering(BucketType.ONLY_VIDEO_BUCKETS)
                    .setImageLoader(glideImageLoader)
                    .build()
            }

            R.id.menuWithCaption -> {
                FalleryBuilder()
                    .setCaptionEnabledOptions(CaptionEnabledOptions(enabled = true))
                    .setImageLoader(glideImageLoader)
                    .build()
            }

            R.id.menuDraculaTheme -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setTheme(ir.mehdiyari.fallery.R.style.Fallery_Dracula)
                    .build()
            }

            R.id.menuLandscapeOrientation -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    .build()
            }

            R.id.menuPreviewScrollOrientation -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setMediaPreviewViewPagerOrientation(ViewPager2.ORIENTATION_VERTICAL)
                    .build()
            }

            R.id.menuMaxSelectable -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setMaxSelectableMedia(5)
                    .build()
            }

            R.id.menuWithCustomVideoToggleOnClick -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setOnVideoPlayClick {
                        Toast.makeText(this, "iam custom on click", Toast.LENGTH_LONG).show()
                    }
                    .build()
            }

            R.id.menuBucketListModeGrid -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setBucketItemModeToggleEnabled(false)
                    .setBucketItemMode(BucketRecyclerViewItemMode.GridStyle)
                    .build()
            }

            R.id.menuBucketListModeVertical -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setBucketItemModeToggleEnabled(false)
                    .setBucketItemMode(BucketRecyclerViewItemMode.LinearStyle)
                    .build()
            }

            R.id.menuWithCustomSelectToggleBackColor -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setSelectedMediaToggleBackgroundColor(Color.RED)
                    .build()
            }

            R.id.menuCustomOnlineGallery -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setContentProviders(
                        CustomOnlineBucketContentProvider(),
                        CustomOnlineBucketProvider()
                    )
                    .setMediaObserverEnabled(false)
                    .build()
            }

            R.id.menuWitCustomCaptionEditText -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setCaptionEnabledOptions(
                        CaptionEnabledOptions(
                            enabled = true, editTextLayoutResId = R.layout.custom_fallery_edit_text
                        )
                    ).build()
            }

            R.id.menuCustomTheme -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setTheme(R.style.Fallery_Blue_Theme)
                    .build()
            }

            R.id.menuSpanCount -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setFallerySpanCountMode(FalleryBucketsSpanCountMode.UserZoomInOrZoomOut)
                    .build()
            }

            else -> FalleryOptions(glideImageLoader)
        }).also {
            falleryActivityResult.launch(it)
        }
    }

    private fun handleFalleryResult(result: List<String>?, caption: String?) {
        listCurrentMedias = mutableListOf<Pair<String, String>>().apply {
            addAll(listCurrentMedias)
            result?.onEach { add(it to (caption ?: "")) }
        }

        visibleRecyclerViewIfCurrentListIsNotEmpty()
        mediaAdapter.submitList(listCurrentMedias)
    }

    private fun animateLayouts() {
        Handler(Looper.getMainLooper()).postDelayed({
            Handler(Looper.getMainLooper()).postDelayed({
                binding.bottomAppBarExample.fabCradleMargin =
                    resources.getDimension(R.dimen.min_fabCradleMargin)
                binding.bottomAppBarExample.fabCradleRoundedCornerRadius =
                    resources.getDimension(R.dimen.min_fabCradleRoundedCornerRadius)
            }, 60)
            binding.fabOpenFallery.startAnimation(
                TranslateAnimation(
                    0F, 0F, 0F, -220F
                ).apply {
                    fillAfter = true
                    duration = 250
                }
            )
        }, 200)

        Handler(Looper.getMainLooper()).postDelayed({
            Handler(Looper.getMainLooper()).postDelayed({
                binding.bottomAppBarExample.fabCradleMargin =
                    resources.getDimension(R.dimen.fabCradleMargin)
                binding.bottomAppBarExample.fabCradleRoundedCornerRadius =
                    resources.getDimension(R.dimen.fabCradleRoundedCornerRadius)
            }, 60)
            binding.fabOpenFallery.startAnimation(
                TranslateAnimation(
                    0F, 0F, -220F, 0F
                ).apply {
                    fillAfter = true
                    duration = 150
                }
            )
        }, 450)
    }

    override fun onDestroy() {
        if (isFinishing) FalleryExample.customGalleryApiService = null
        binding.bottomAppBarExample.setNavigationOnClickListener(null)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("itemIdSelected", itemIdSelected)
        outState.putStringArray(
            "results",
            listCurrentMedias.map { "${it.first}~|~${it.second}" }.toTypedArray()
        )
        super.onSaveInstanceState(outState)
    }
}