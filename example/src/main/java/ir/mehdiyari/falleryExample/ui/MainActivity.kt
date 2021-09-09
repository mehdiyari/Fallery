package ir.mehdiyari.falleryExample.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import ir.mehdiyari.fallery.main.fallery.*
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.falleryExample.R
import ir.mehdiyari.falleryExample.ui.customGallery.CustomOnlineBucketContentProvider
import ir.mehdiyari.falleryExample.ui.customGallery.CustomOnlineBucketProvider
import ir.mehdiyari.falleryExample.utils.FalleryExample
import ir.mehdiyari.falleryExample.utils.GlideImageLoader
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val fileProviderAuthority by lazy { "${application.packageName}.provider" }
    private var itemIdSelected: Int = R.id.menuDefaultOptions
    private val falleryRequestCode = 830
    private val mediaAdapter by lazy { MediaAdapter() }
    private var listCurrentMedias = listOf<Pair<String, String>>()
    private val glideImageLoader by lazy { GlideImageLoader() }

    private val bottomNavigationDrawerFragment by lazy {
        BottomNavigationDrawerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        bottomNavigationDrawerFragment.onMenuItemSelected = { itemSelected ->
            itemIdSelected = itemSelected
            animateLayouts()
        }

        bottomAppBarExample.setOnMenuItemClickListener {
            if (it.itemId == R.id.menuClear) {
                listCurrentMedias = listOf()
                mediaAdapter.submitList(listCurrentMedias)
                visibleRecyclerViewIfCurrentListIsNotEmpty()
            }

            true
        }

        bottomAppBarExample.setNavigationOnClickListener {
            if (!bottomNavigationDrawerFragment.isAdded) {
                bottomNavigationDrawerFragment.selectedItemId = itemIdSelected
                bottomNavigationDrawerFragment.show(supportFragmentManager, "bndf")
            }
        }

        fabOpenFallery.setOnClickListener {
            openFalleryBasedOnSelectedMode()
        }


        visibleRecyclerViewIfCurrentListIsNotEmpty()
        mediaAdapter.submitList(listCurrentMedias)
        recyclerViewResults.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = mediaAdapter
        }
    }

    private fun visibleRecyclerViewIfCurrentListIsNotEmpty() {
        if (listCurrentMedias.isEmpty()) {
            textViewNotSelectedMediaMessage.visibility = View.VISIBLE
            recyclerViewResults.visibility = View.GONE
        } else {
            textViewNotSelectedMediaMessage.visibility = View.GONE
            recyclerViewResults.visibility = View.VISIBLE
        }
    }

    private fun openFalleryBasedOnSelectedMode() {
        (when (itemIdSelected) {
            R.id.menuDefaultOptions -> FalleryOptions(glideImageLoader)
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
                    .setTheme(R.style.Fallery_Dracula)
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
                    .setContentProviders(CustomOnlineBucketContentProvider(), CustomOnlineBucketProvider())
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
            R.id.menuGrantExternalStoragePermission -> {
                val builder = FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setGrantExternalStoragePermission(false)
                    .build()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this@MainActivity, "Please grant external storage permission", Toast.LENGTH_SHORT).show()
                        requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 10)
                        null
                    } else {
                        builder
                    }
                } else {
                    builder
                }
            }
            R.id.menuGrantSharedStoragePermission -> {
                val builder = FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setGrantSharedStoragePermission(false)
                    .build()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this@MainActivity, "Please grant external storage permission", Toast.LENGTH_SHORT).show()
                        requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 10)
                        null
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageManager()) {
                            Toast.makeText(this@MainActivity, "App: Please grant shared storage permission before starting fallery", Toast.LENGTH_SHORT).show()
                            Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).also {
                                it.data = Uri.fromParts("package", packageName, null)
                                startActivity(it)
                            }
                            null
                        } else {
                            builder
                        }
                    }
                } else {
                    builder
                }
            }
            R.id.menuCustomTheme -> {
                FalleryBuilder()
                    .setImageLoader(glideImageLoader)
                    .setTheme(R.style.Fallery_Blue_Theme)
                    .build()
            }
            else -> FalleryOptions(glideImageLoader)
        })?.also {
            startFalleryWithOptions(falleryRequestCode, it)
        }
    }

    private fun animateLayouts() {
        Handler().postDelayed({
            Handler().postDelayed({
                bottomAppBarExample.fabCradleMargin = resources.getDimension(R.dimen.min_fabCradleMargin)
                bottomAppBarExample.fabCradleRoundedCornerRadius = resources.getDimension(R.dimen.min_fabCradleRoundedCornerRadius)
            }, 60)
            fabOpenFallery.startAnimation(
                TranslateAnimation(
                    0F, 0F, 0F, -220F
                ).apply {
                    fillAfter = true
                    duration = 250
                }
            )
        }, 200)

        Handler().postDelayed({
            Handler().postDelayed({
                bottomAppBarExample.fabCradleMargin = resources.getDimension(R.dimen.fabCradleMargin)
                bottomAppBarExample.fabCradleRoundedCornerRadius = resources.getDimension(R.dimen.fabCradleRoundedCornerRadius)
            }, 60)
            fabOpenFallery.startAnimation(
                TranslateAnimation(
                    0F, 0F, -220F, 0F
                ).apply {
                    fillAfter = true
                    duration = 150
                }
            )
        }, 450)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == falleryRequestCode) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                listCurrentMedias = mutableListOf<Pair<String, String>>().apply {
                    addAll(listCurrentMedias)
                    val caption = data.getFalleryCaptionFromIntent()
                    data.getFalleryResultMediasFromIntent()?.also { results ->
                        results.forEach {
                            add(it to (caption ?: ""))
                        }
                    }
                }

                visibleRecyclerViewIfCurrentListIsNotEmpty()
                mediaAdapter.submitList(listCurrentMedias)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        if (isFinishing) FalleryExample.customGalleryApiService = null
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("itemIdSelected", itemIdSelected)
        outState.putStringArray("results", listCurrentMedias.map { "${it.first}~|~${it.second}" }.toTypedArray())
        super.onSaveInstanceState(outState)
    }
}