package ir.mehdiyari.fallery.main.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.buckets.ui.bucketContent.BucketContentFragment
import ir.mehdiyari.fallery.buckets.ui.bucketList.BucketListFragment
import ir.mehdiyari.fallery.main.fallery.BucketRecyclerViewItemMode
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.main.di.FalleryCoreComponentHolder
import ir.mehdiyari.fallery.utils.MediaStoreObserver
import ir.mehdiyari.fallery.utils.WRITE_EXTERNAL_REQUEST_CODE
import ir.mehdiyari.fallery.utils.getSettingIntent
import ir.mehdiyari.fallery.utils.permissionChecker
import kotlinx.android.synthetic.main.activity_fallery.*
import kotlinx.android.synthetic.main.caption_layout.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class FalleryActivity : AppCompatActivity(), MediaObserverInterface {

    private lateinit var falleryViewModel: FalleryViewModel
    private val mediaStoreObserver by lazy { MediaStoreObserver(Handler(), this) }

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        FalleryActivityComponentHolder.createOrGetComponent(this)
        setTheme(FalleryCoreComponentHolder.getOrThrow().provideFalleryOptions().themeResId)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fallery)
        initViewModel()
        initialize()
        initView()
    }

    private fun initialize() {
        permissionChecker(Manifest.permission.WRITE_EXTERNAL_STORAGE, granted = {
            falleryViewModel.storagePermissionGranted()
        }, denied = {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_REQUEST_CODE)
        })
    }


    @ExperimentalCoroutinesApi
    private fun initViewModel() {
        falleryViewModel = ViewModelProvider(
            this,
            FalleryActivityComponentHolder.createOrGetComponent(this).provideFalleryViewModelFactory()
        )[FalleryViewModel::class.java].apply {
            captionEnabledLiveData.observe(this@FalleryActivity, Observer {
                if (it)
                    showCaptionLayout(withAnim = true)
                else
                    hideCaptionLayout(withAnim = true)
            })

            lifecycleScope.launch {
                mediaCountStateFlow.collect {
                    setupMediaCountView(it)
                }
            }
        }

        falleryViewModel.currentFragmentLiveData.observeSingleEvent(this@FalleryActivity, Observer { falleryView ->
            when (falleryView) {
                is FalleryView.BucketList -> {
                    supportFragmentManager.beginTransaction()
                        .add(R.id.frameLayoutFragmentContainer, BucketListFragment())
                        .commit()
                }
                is FalleryView.BucketContent -> supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayoutFragmentContainer, BucketContentFragment().apply {
                        arguments = Bundle().apply {
                            putLong("bucket_id", falleryView.bucketId)
                        }
                    })
                    .addToBackStack(null)
                    .commit()
                is FalleryView.PhotoPreview -> TODO()
                else -> Unit
            }

        })
    }

    private fun initView() {
        addCameraMenuItem()
        addRecyclerViewItemViewModeMenuItem()
        toolbarFalleryActivity.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.bucketListMenuItemShowRecyclerViewItemModelChanger -> {
                    FalleryCoreComponentHolder.getOrThrow().provideFalleryOptions().also { falleryOptions ->
                        if (falleryOptions.bucketRecyclerViewItemMode == BucketRecyclerViewItemMode.LinearStyle) {
                            falleryOptions.bucketRecyclerViewItemMode = BucketRecyclerViewItemMode.GridStyle
                        } else
                            falleryOptions.bucketRecyclerViewItemMode = BucketRecyclerViewItemMode.LinearStyle

                        it.icon = getRecyclerViewItemViewModeIcon(falleryOptions.bucketRecyclerViewItemMode)
                        falleryViewModel.changeRecyclerViewItemMode(falleryOptions.bucketRecyclerViewItemMode)
                    }
                }
            }

            true
        }
    }

    private fun addCameraMenuItem() {
        toolbarFalleryActivity.apply {
            FalleryCoreComponentHolder.getOrThrow().provideFalleryOptions().cameraEnabledOptions.also {
                if (it.enabled) {
                    menu.add(0, R.id.bucketListMenuItemCamera, 1, R.string.camera).apply {
                        setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                        icon = AppCompatResources.getDrawable(
                            this@FalleryActivity,
                            R.drawable.fallery_icon_camera
                        )?.also(::setToolbarColorToMenuItemDrawable)
                    }
                }
            }
        }
    }

    private fun getRecyclerViewItemViewModeIcon(mode: BucketRecyclerViewItemMode?): Drawable? {
        return AppCompatResources.getDrawable(
            this@FalleryActivity,
            if (mode?.value != R.layout.grid_bucket_item_view) R.drawable.fallery_grid_mode else R.drawable.fallery_linear_mode
        )?.also(::setToolbarColorToMenuItemDrawable)
    }

    // using BlendModeColorFilter for android Q and above. and setColorFilter for below of android Q
    @Suppress("DEPRECATION")
    private fun setToolbarColorToMenuItemDrawable(drawable: Drawable) {
        val tintColor = FalleryActivityComponentHolder.getOrNull()?.provideFalleryStyleAttrs()?.falleryToolbarIconTintColor ?: Color.BLACK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(
                tintColor,
                BlendMode.SRC_IN
            )
        } else
            drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
    }

    private fun addRecyclerViewItemViewModeMenuItem() {
        toolbarFalleryActivity.apply {
            FalleryCoreComponentHolder.getOrThrow().provideFalleryOptions().changeBucketRecyclerViewItemModeByToolbarIcon.also {
                val mode = FalleryActivityComponentHolder.getOrNull()?.provideFalleryOptions()?.bucketRecyclerViewItemMode
                if (it) {
                    menu.add(0, R.id.bucketListMenuItemShowRecyclerViewItemModelChanger, 0, R.string.list_mode).apply {
                        setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                        icon = getRecyclerViewItemViewModeIcon(mode)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && requestCode == WRITE_EXTERNAL_REQUEST_CODE && grantResults.first() == PackageManager.PERMISSION_GRANTED)
            initialize()
        else if (grantResults.isNotEmpty() && requestCode == WRITE_EXTERNAL_REQUEST_CODE && grantResults.first() == PackageManager.PERMISSION_DENIED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this@FalleryActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                showPermanentlyPermissionDeniedDialog()
            else
                writeExternalStoragePermissionDenied()
        }
    }

    private fun writeExternalStoragePermissionDenied() {
        AlertDialog.Builder(this@FalleryActivity, R.style.Fallery_AlertDialogTheme)
            .setMessage(R.string.access_external_storage_denied)
            .setCancelable(false)
            .setPositiveButton(R.string.continue_button) { dialog, _ ->
                dialog.dismiss()
                initialize()
            }.setNegativeButton(R.string.exit_button) { dialog, _ ->
                dialog.dismiss()
                onBackPressed()
            }
            .show()
    }

    private fun showPermanentlyPermissionDeniedDialog() {
        AlertDialog.Builder(this@FalleryActivity, R.style.Fallery_AlertDialogTheme)
            .setMessage(R.string.access_external_storage_permanently_denied)
            .setCancelable(false)
            .setPositiveButton(R.string.continue_button) { dialog, _ ->
                dialog.dismiss()
                try {
                    startActivity(getSettingIntent(this@FalleryActivity.applicationContext))
                } catch (activityNotFoundException: ActivityNotFoundException) {
                    activityNotFoundException.printStackTrace()
                    onBackPressed()
                }
            }.setNegativeButton(R.string.cancel_button) { dialog, _ ->
                dialog.dismiss()
                onBackPressed()
            }
            .show()
    }

    private fun hideCaptionLayout(withAnim: Boolean) {
        prepareCaptionViewStub()
        if (relativeLayoutCaptionLayout?.visibility == View.GONE) return
        frameLayoutCaptionHolder.findViewById<EditText>(R.id.falleryEditTextCaption)?.hideKeyboard()
        if (!withAnim)
            relativeLayoutCaptionLayout?.visibility = View.GONE
        else {
            relativeLayoutCaptionLayout?.startAnimation(
                TranslateAnimation(
                    0f, 0f, 0f, (relativeLayoutCaptionLayout?.height)?.toFloat() ?: 0f
                ).apply {
                    fillAfter = true
                    duration = 250
                    setOnAnimationEndListener {
                        relativeLayoutCaptionLayout.visibility = View.GONE
                        relativeLayoutCaptionLayout.animation = null
                    }
                }
            )
        }
    }

    private fun showCaptionLayout(withAnim: Boolean) {
        prepareCaptionViewStub()
        if (relativeLayoutCaptionLayout?.visibility == View.VISIBLE) return
        if (!withAnim)
            relativeLayoutCaptionLayout?.visibility = View.VISIBLE
        else {
            relativeLayoutCaptionLayout.visibility = View.VISIBLE
            relativeLayoutCaptionLayout?.startAnimation(
                TranslateAnimation(
                    0f, 0f, ((relativeLayoutCaptionLayout?.height)?.toFloat() ?: 0f), 0f
                ).apply {
                    fillAfter = true
                    duration = 250
                    setOnAnimationEndListener {
                        relativeLayoutCaptionLayout.animation = null
                    }
                }
            )
        }
    }

    private fun prepareCaptionViewStub() {
        if (viewStubCaptionLayout != null && viewStubCaptionLayout.parent != null) {
            (try {
                viewStubCaptionLayout.inflate()
                falleryOptions.captionEnabledOptions.editTextLayoutResId.let {
                    LayoutInflater.from(this).inflate(it, frameLayoutCaptionHolder, false).findViewById<AppCompatEditText>(R.id.falleryEditTextCaption)
                }
            } catch (ignored: Throwable) {
                Log.e(FALLERY_LOG_TAG, "error while inflating captionLayoutResId. switch to default implementation")
                LayoutInflater.from(this).inflate(R.layout.caption_edit_text_layout, frameLayoutCaptionHolder, false)
                    .findViewById<AppCompatEditText>(R.id.falleryEditTextCaption)
            }).also {
                frameLayoutCaptionHolder.addView(it)
            }
        }

    }

    override fun onDestroy() {
        if (isFinishing) {
            FalleryActivityComponentHolder.onDestroy()
            FalleryCoreComponentHolder.onDestroy()
        }

        super.onDestroy()
    }

    override fun getMediaObserverInstance(): MediaStoreObserver? = if (
        FalleryCoreComponentHolder.getOrThrow().provideFalleryOptions().mediaObserverEnabled)
        mediaStoreObserver
    else
        null
}