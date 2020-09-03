package ir.mehdiyari.fallery.main.ui

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.buckets.bucketContent.BaseBucketContentFragment
import ir.mehdiyari.fallery.buckets.bucketList.BucketListFragment
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.main.di.FalleryCoreComponentHolder
import ir.mehdiyari.fallery.main.fallery.BucketRecyclerViewItemMode
import ir.mehdiyari.fallery.utils.*
import kotlinx.android.synthetic.main.activity_fallery.*
import kotlinx.android.synthetic.main.caption_layout.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.WeakReference

@OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
internal class FalleryActivity : AppCompatActivity(), MediaObserverInterface, FalleryToolbarVisibilityController {

    private lateinit var falleryViewModel: FalleryViewModel
    private val mediaStoreObserver by lazy { MediaStoreObserver(Handler(), WeakReference(this)) }
    private val falleryOptions by lazy { FalleryActivityComponentHolder.createOrGetComponent(this).provideFalleryOptions() }

    override fun onCreate(savedInstanceState: Bundle?) {
        FalleryActivityComponentHolder.createOrGetComponent(this)
        requestedOrientation = falleryOptions.orientationMode
        setTheme(falleryOptions.themeResId)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fallery)
        initViewModel()
        initialize()
        initView()
    }

    private fun initialize() {
        if (!falleryOptions.grantExternalStoragePermission) {
            falleryViewModel.storagePermissionGranted()
        } else {
            permissionChecker(Manifest.permission.WRITE_EXTERNAL_STORAGE, granted = {
                falleryViewModel.storagePermissionGranted()
            }, denied = {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_REQUEST_CODE)
            })
        }
    }


    private fun initViewModel() {
        falleryViewModel = ViewModelProvider(
            this,
            FalleryActivityComponentHolder.createOrGetComponent(this).provideFalleryViewModelFactory()
        )[FalleryViewModel::class.java]

        falleryViewModel.apply {
            showErrorSingleLiveEvent.observe(this@FalleryActivity, Observer {
                if (it != null && it == R.string.fallery_error_max_selectable) {
                    Toast.makeText(this@FalleryActivity, getString(it, falleryOptions.maxSelectableMedia), Toast.LENGTH_SHORT).show()
                }
            })

            resultSingleLiveEvent.observe(this@FalleryActivity, Observer {
                if (it != null && it.isNotEmpty()) {
                    finishWithOKResult(it)
                } else {
                    finishWithCancelResult()
                }
            })

            lifecycleScope.launch {
                launch {
                    sendActionEnabledStateFlow.mapNotNull { it }.collect {
                        if (it)
                            showSendButton()
                        else
                            hideSendButton()
                    }
                }

                captionEnabledStateFlow.mapNotNull { it }.collect {
                    if (it)
                        showCaptionLayout(withAnim = true)
                    else
                        hideCaptionLayout(withAnim = true)
                }
            }

            lifecycleScope.launch {
                mediaCountStateFlow.collect {
                    setupMediaCountView(it)
                }
            }
        }

        observeMediaStopChanges()

        falleryViewModel.currentFragmentLiveData.observe(this@FalleryActivity, Observer { falleryView ->
            when (falleryView) {
                is FalleryView.BucketList -> {
                    toolbarFalleryActivity.title = getString(falleryOptions.toolbarTitle)
                    supportFragmentManager.beginTransaction()
                        .add(R.id.layoutFragmentContainer, BucketListFragment())
                        .commit()
                    toolbarFalleryActivity.menu?.findItem(R.id.bucketListMenuItemShowRecyclerViewItemModelChanger)?.isVisible = true
                }
                is FalleryView.BucketContent -> {
                    toolbarFalleryActivity.title = falleryView.bucketName
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.layoutFragmentContainer, BaseBucketContentFragment().apply {
                            arguments = Bundle().apply {
                                putLong("bucket_id", falleryView.bucketId)
                            }
                        })
                        .addToBackStack(null)
                        .commit()
                    toolbarFalleryActivity.menu?.findItem(R.id.bucketListMenuItemShowRecyclerViewItemModelChanger)?.isVisible = false
                }
                else -> Unit
            }

        })
    }

    private fun setupMediaCountView(value: MediaCountModel) {
        if (value.selectedCount <= 0) {
            toolbarFalleryActivity.title =
                if (falleryViewModel.currentFragmentLiveData.value is FalleryView.BucketContent) falleryViewModel.currentFragmentLiveData.value!!.let { it as FalleryView.BucketContent }.bucketName else getString(
                    falleryOptions.toolbarTitle
                )
            toolbarFalleryActivity.setNavigationIcon(R.drawable.fallery_ic_back_arrow)
            toolbarFalleryActivity.setNavigationOnClickListener { onBackPressed() }
        } else {
            toolbarFalleryActivity.setNavigationIcon(R.drawable.fallery_ic_cancel)
            toolbarFalleryActivity.setNavigationOnClickListener { falleryViewModel.deselectAllSelections() }
            toolbarFalleryActivity.title = createMediaCountSpannable(
                context = this,
                value = value,
                colorAccent = FalleryActivityComponentHolder.getOrNull()?.provideFalleryStyleAttrs()?.falleryColorAccent ?: Color.BLUE
            )
        }
    }

    private fun initView() {
        addCameraMenuItem()
        addRecyclerViewItemViewModeMenuItem()
        toolbarFalleryActivity.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.bucketListMenuItemShowRecyclerViewItemModelChanger -> {

                    if (falleryOptions.bucketRecyclerViewItemMode == BucketRecyclerViewItemMode.LinearStyle) {
                        falleryOptions.bucketRecyclerViewItemMode = BucketRecyclerViewItemMode.GridStyle
                    } else
                        falleryOptions.bucketRecyclerViewItemMode = BucketRecyclerViewItemMode.LinearStyle

                    it.icon = getRecyclerViewItemViewModeIcon(falleryOptions.bucketRecyclerViewItemMode)
                    falleryViewModel.changeRecyclerViewItemMode(falleryOptions.bucketRecyclerViewItemMode)

                }
                R.id.bucketListMenuItemCamera -> takePhoto()
            }

            true
        }

        floatingButtonSendMedia.setOnClickListener {
            falleryViewModel.prepareSelectedResults()
        }
    }

    private fun addCameraMenuItem() {
        toolbarFalleryActivity.apply {
            falleryOptions.cameraEnabledOptions.also {
                if (it.enabled) {
                    if (it.fileProviderAuthority != null) {
                        menu.add(0, R.id.bucketListMenuItemCamera, 1, R.string.camera).apply {
                            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                            icon = AppCompatResources.getDrawable(
                                this@FalleryActivity,
                                R.drawable.fallery_icon_camera
                            )?.also(::setToolbarColorToMenuItemDrawable)
                        }
                    } else {
                        throw IllegalArgumentException("cant taking photo without fileProviderAuthority")
                    }
                }
            }
        }
    }


    private fun takePhoto() {
        falleryOptions.cameraEnabledOptions.also {
            val filename = generatePhotoFilename()
            (if (it.directory != null) {
                File(it.directory, filename)
            } else {
                File(FalleryActivityComponentHolder.getOrNull()!!.provideCacheDir().cacheDir, filename)
            }).also { temporaryFile ->
                getIntentForTakingPhoto(it.fileProviderAuthority!!, temporaryFile)?.also { takePhotoIntent ->
                    startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST_CODE)
                    falleryViewModel.setCameraPhotoFileAddress(temporaryFile.path)
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
            falleryOptions.bucketItemModeToggleEnabled.also {
                val mode = falleryOptions.bucketRecyclerViewItemMode
                if (it) {
                    menu.add(0, R.id.bucketListMenuItemShowRecyclerViewItemModelChanger, 0, R.string.list_mode).apply {
                        setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                        icon = getRecyclerViewItemViewModeIcon(mode)
                    }

                    showOrHideMenusBasedOnFragment()
                }
            }
        }
    }

    private fun showOrHideMenusBasedOnFragment() {
        try {
            toolbarFalleryActivity.menu?.findItem(R.id.bucketListMenuItemShowRecyclerViewItemModelChanger)?.isVisible =
                supportFragmentManager.findFragmentById(R.id.layoutFragmentContainer) is BucketListFragment
        } catch (ignored: Throwable) {
            ignored.printStackTrace()
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

    @Suppress("SameParameterValue")
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

    @Suppress("SameParameterValue")
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
                imageViewSendMessage.setOnClickListener { falleryViewModel.prepareSelectedResults() }
                falleryOptions.captionEnabledOptions.editTextLayoutResId.let {
                    LayoutInflater.from(this).inflate(it, frameLayoutCaptionHolder, false).findViewById<AppCompatEditText>(R.id.falleryEditTextCaption).apply {
                        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    }
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

    private fun observeMediaStopChanges() {
        if (falleryOptions.mediaObserverEnabled) {
            getMediaObserverInstance()?.externalStorageChangeLiveData?.observe(this, Observer {
                if (!falleryOptions.grantExternalStoragePermission) {
                    falleryViewModel.validateSelections()
                } else {
                    permissionChecker(Manifest.permission.WRITE_EXTERNAL_STORAGE, granted = {
                        falleryViewModel.validateSelections()
                    }, denied = {
                        Log.e(FALLERY_LOG_TAG, "mediaStoreObserver -> getMedias -> app has not access to external storage for get medias of bucket from mediaStore")
                    })
                }
            })
        }
    }

    override fun onDestroy() {
        FalleryActivityComponentHolder.onDestroy()
        if (isFinishing) {
            FalleryCoreComponentHolder.onDestroy()
        }
        super.onDestroy()
    }

    override fun getMediaObserverInstance(): MediaStoreObserver? = if (
        FalleryCoreComponentHolder.getOrThrow().provideFalleryOptions().mediaObserverEnabled)
        mediaStoreObserver
    else
        null

    private fun showSendButton(withAnim: Boolean = true) {
        if (frameLayoutSendMedia?.visibility == View.VISIBLE) return

        if (!withAnim) {
            frameLayoutSendMedia?.visibility = View.GONE
            return
        }


        floatingButtonSendMedia.visibility = View.VISIBLE
        floatingButtonSendMedia.startAnimation(
            TranslateAnimation(
                ((floatingButtonSendMedia?.height)?.toFloat() ?: 0f), 0f, 0f, 0f
            ).apply {
                fillAfter = true
                duration = 200
                setOnAnimationEndListener {
                    floatingButtonSendMedia.animation = null
                }
            }
        )

        frameLayoutSendMedia.visibility = View.VISIBLE
        frameLayoutSendMedia?.startAnimation(
            TranslateAnimation(
                0f, 0f, ((frameLayoutSendMedia?.height)?.toFloat() ?: 0f), 0f
            ).apply {
                fillAfter = true
                duration = 200
                setOnAnimationEndListener {
                    frameLayoutSendMedia.animation = null
                }
            }
        )
    }

    private fun hideSendButton(withAnim: Boolean = true) {
        if (frameLayoutSendMedia?.visibility == View.GONE) return

        if (!withAnim) {
            frameLayoutSendMedia?.visibility = View.VISIBLE
            return
        }

        floatingButtonSendMedia.startAnimation(
            TranslateAnimation(
                0f, ((floatingButtonSendMedia?.height)?.toFloat() ?: 0f), 0f, 0f
            ).apply {
                fillAfter = true
                duration = 200
                setOnAnimationEndListener {
                    floatingButtonSendMedia.visibility = View.GONE
                    floatingButtonSendMedia.animation = null
                }
            }
        )

        frameLayoutSendMedia?.startAnimation(
            TranslateAnimation(
                0f, 0f, 0f, ((frameLayoutSendMedia?.height)?.toFloat() ?: 0f)
            ).apply {
                fillAfter = true
                duration = 200
                setOnAnimationEndListener {
                    frameLayoutSendMedia.visibility = View.GONE
                    frameLayoutSendMedia.animation = null
                }
            }
        )
    }


    override fun showToolbar(withAnim: Boolean) {
        if (toolbarFalleryActivity.visibility == View.VISIBLE) return
        fun setHeightOfFragmentContainer() {
            layoutFragmentContainer.layoutParams = (layoutFragmentContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            }
        }
        if (!withAnim) {
            setHeightOfFragmentContainer()
            toolbarFalleryActivity?.visibility = View.VISIBLE
            return
        }

        toolbarFalleryActivity.startAnimation(TranslateAnimation(0f, 0f, -toolbarFalleryActivity.height.toFloat(), 0f).apply {
            duration = 200
            fillAfter = true
            setOnAnimationEndListener {
                setHeightOfFragmentContainer()
                toolbarFalleryActivity.visibility = View.VISIBLE
                toolbarFalleryActivity.animation = null
            }
        })
    }

    override fun hideToolbar(withAnim: Boolean) {
        if (toolbarFalleryActivity.visibility == View.GONE) return
        fun setHeightOfFragmentContainer() {
            layoutFragmentContainer.layoutParams = (layoutFragmentContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                height = ConstraintLayout.LayoutParams.MATCH_PARENT
            }
        }
        if (!withAnim) {
            setHeightOfFragmentContainer()
            toolbarFalleryActivity?.visibility = View.GONE
            return
        }

        toolbarFalleryActivity.startAnimation(TranslateAnimation(0f, 0f, 0f, -toolbarFalleryActivity.height.toFloat()).apply {
            duration = 200
            fillAfter = true
            setOnAnimationEndListener {
                setHeightOfFragmentContainer()
                toolbarFalleryActivity.visibility = View.GONE
                toolbarFalleryActivity.animation = null
            }
        })
    }

    override fun onBackPressed() {
        for (fragment in supportFragmentManager.fragments) {
            if (fragment.isVisible && fragment.childFragmentManager.backStackEntryCount > 1) {
                fragment.childFragmentManager.popBackStack()
                return
            }
        }

        if (supportFragmentManager.findFragmentById(R.id.layoutFragmentContainer) is BucketListFragment && falleryViewModel.userSelectedMedias)
            falleryViewModel.deselectAllSelections()
        else if (supportFragmentManager.findFragmentById(R.id.layoutFragmentContainer) is BaseBucketContentFragment) {
            super.onBackPressed()
            if (!falleryViewModel.userSelectedMedias) {
                toolbarFalleryActivity.title = getString(falleryOptions.toolbarTitle)
            }
            falleryViewModel.clearLatestValueOfCurrentFragmentLiveData()
        } else {
            super.onBackPressed()
        }

        showOrHideMenusBasedOnFragment()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                handleTakingPhotoResult()
            } else {
                falleryViewModel.clearCameraPhotoFileAddress()
            }
        }
    }

    private fun handleTakingPhotoResult() {
        falleryViewModel.prepareCameraResultWithSelectedResults()
    }

    private fun finishWithCancelResult() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun finishWithOKResult(it: Array<String>) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(FALLERY_MEDIAS_LIST_KEY, it)
            if (falleryOptions.captionEnabledOptions.enabled) {
                putExtra(FALLERY_CAPTION_KEY, frameLayoutCaptionHolder.findViewById<EditText>(R.id.falleryEditTextCaption)?.text?.toString()?.trim())
            }
        })
        finish()
    }
}