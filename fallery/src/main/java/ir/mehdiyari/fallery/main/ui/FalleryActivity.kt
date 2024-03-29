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
import ir.mehdiyari.fallery.databinding.ActivityFalleryBinding
import ir.mehdiyari.fallery.databinding.CaptionLayoutBinding
import ir.mehdiyari.fallery.main.di.FalleryActivityComponentHolder
import ir.mehdiyari.fallery.main.di.FalleryCoreComponentHolder
import ir.mehdiyari.fallery.main.fallery.BucketRecyclerViewItemMode
import ir.mehdiyari.fallery.utils.FALLERY_CAPTION_KEY
import ir.mehdiyari.fallery.utils.FALLERY_LOG_TAG
import ir.mehdiyari.fallery.utils.FALLERY_MEDIAS_LIST_KEY
import ir.mehdiyari.fallery.utils.TAKE_PHOTO_REQUEST_CODE
import ir.mehdiyari.fallery.utils.WRITE_EXTERNAL_REQUEST_CODE
import ir.mehdiyari.fallery.utils.createMediaCountSpannable
import ir.mehdiyari.fallery.utils.generatePhotoFilename
import ir.mehdiyari.fallery.utils.getIntentForTakingPhoto
import ir.mehdiyari.fallery.utils.getSettingIntent
import ir.mehdiyari.fallery.utils.hideKeyboard
import ir.mehdiyari.fallery.utils.permissionChecker
import ir.mehdiyari.fallery.utils.setOnAnimationEndListener
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import java.io.File

internal class FalleryActivity : AppCompatActivity(), FalleryToolbarVisibilityController {

    private var _binding: ActivityFalleryBinding? = null
    private val binding get() = _binding!!

    private var _captionBinding: CaptionLayoutBinding? = null
    private val captionBinding get() = _captionBinding!!

    private lateinit var falleryViewModel: FalleryViewModel
    private val falleryOptions by lazy {
        FalleryActivityComponentHolder.createOrGetComponent(this).provideFalleryOptions()
    }
    private var frameLayoutSendMediaAnimationPostRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            FalleryCoreComponentHolder.getOrThrow()
        } catch (t: Throwable) {
            finish()
        }
        FalleryActivityComponentHolder.createOrGetComponent(this)
        requestedOrientation = falleryOptions.orientationMode
        setTheme(falleryOptions.themeResId)
        super.onCreate(savedInstanceState)
        ActivityFalleryBinding.inflate(layoutInflater).also {
            setContentView(it.root)
            _binding = it
            binding.viewStubCaptionLayout.setOnInflateListener { _, view ->
                _captionBinding = CaptionLayoutBinding.bind(view)
            }
        }
        initViewModel()
        initialize()
        initView()
    }

    override fun onStart() {
        super.onStart()
        if (falleryViewModel.shouldInitializeAfterStart) {
            falleryViewModel.shouldInitializeAfterStart = false
            initialize()
        }
    }

    private fun initialize() {
        if (!falleryOptions.grantExternalStoragePermission) {
            falleryViewModel.storagePermissionGranted()
        } else {
            val permissions = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                }
            } else {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            permissionChecker(permissions.toTypedArray(), onAllGranted = {
                falleryViewModel.storagePermissionGranted()
            }, onDenied = {
                requestPermissions(
                    permissions.toTypedArray(),
                    WRITE_EXTERNAL_REQUEST_CODE
                )
            })
        }
    }

    private fun initViewModel() {
        falleryViewModel = ViewModelProvider(
            this,
            FalleryActivityComponentHolder.createOrGetComponent(this)
                .provideFalleryViewModelFactory()
        )[FalleryViewModel::class.java]

        falleryViewModel.apply {
            showErrorSingleLiveEvent.observe(
                this@FalleryActivity,
                Observer(this@FalleryActivity::showError)
            )
            resultSingleLiveEvent.observe(
                this@FalleryActivity,
                Observer(this@FalleryActivity::handleFalleryResults)
            )

            lifecycleScope.launch {
                launch {
                    sendActionEnabledStateFlow.mapNotNull { it }.collect {
                        if (it)
                            showSendButton()
                        else
                            hideSendButton()
                    }
                }

                launch {
                    captionEnabledStateFlow.mapNotNull { it }.collect {
                        if (it)
                            showCaptionLayout(withAnim = true)
                        else
                            hideCaptionLayout(withAnim = true)
                    }
                }

                launch {
                    mediaCountStateFlow.collect {
                        setupMediaCountView(it)
                    }
                }
            }
        }

        falleryViewModel.currentFragmentLiveData.observe(this@FalleryActivity) { falleryView ->
            replaceFragment(falleryView)
        }
    }

    private fun handleFalleryResults(it: Array<String>?) {
        if (!it.isNullOrEmpty()) {
            finishWithOKResult(it)
        } else {
            finishWithCancelResult()
        }
    }

    private fun showError(it: Int) {
        if (it == R.string.fallery_error_max_selectable) {
            Toast.makeText(
                this@FalleryActivity,
                getString(it, falleryOptions.maxSelectableMedia),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun replaceFragment(falleryView: FalleryView?) {
        when (falleryView) {
            is FalleryView.BucketList -> {
                if (!falleryViewModel.userSelectedMedias)
                    binding.toolbarFalleryActivity.title = getString(falleryOptions.toolbarTitle)

                supportFragmentManager.beginTransaction()
                    .add(R.id.layoutFragmentContainer, BucketListFragment())
                    .commit()
                binding.toolbarFalleryActivity.menu?.findItem(R.id.bucketListMenuItemShowRecyclerViewItemModelChanger)?.isVisible =
                    true
            }

            is FalleryView.BucketContent -> {
                if (!falleryViewModel.userSelectedMedias)
                    binding.toolbarFalleryActivity.title = falleryView.bucketName

                supportFragmentManager.beginTransaction()
                    .replace(R.id.layoutFragmentContainer, BaseBucketContentFragment().apply {
                        arguments = Bundle().apply {
                            putLong("bucket_id", falleryView.bucketId)
                        }
                    })
                    .addToBackStack(null)
                    .commit()
                binding.toolbarFalleryActivity.menu?.findItem(R.id.bucketListMenuItemShowRecyclerViewItemModelChanger)?.isVisible =
                    false
            }

            else -> Unit
        }
    }

    private fun setupMediaCountView(value: MediaCountModel) {
        if (value.selectedCount <= 0) {
            binding.toolbarFalleryActivity.title =
                if (falleryViewModel.currentFragmentLiveData.value is FalleryView.BucketContent) falleryViewModel.currentFragmentLiveData.value!!.let { it as FalleryView.BucketContent }.bucketName else getString(
                    falleryOptions.toolbarTitle
                )
            binding.toolbarFalleryActivity.setNavigationIcon(R.drawable.fallery_ic_back_arrow)
            binding.toolbarFalleryActivity.setNavigationOnClickListener { onBackPressed() }
        } else {
            binding.toolbarFalleryActivity.setNavigationIcon(R.drawable.fallery_ic_cancel)
            binding.toolbarFalleryActivity.setNavigationOnClickListener { falleryViewModel.deselectAllSelections() }
            binding.toolbarFalleryActivity.title = createMediaCountSpannable(
                context = this,
                value = value,
                colorAccent = FalleryActivityComponentHolder.getOrNull()
                    ?.provideFalleryStyleAttrs()?.falleryColorAccent ?: Color.BLUE
            )
        }
    }

    private fun initView() {
        addCameraMenuItem()
        addRecyclerViewItemViewModeMenuItem()
        binding.toolbarFalleryActivity.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.bucketListMenuItemShowRecyclerViewItemModelChanger -> {

                    if (falleryOptions.bucketRecyclerViewItemMode == BucketRecyclerViewItemMode.LinearStyle) {
                        falleryOptions.bucketRecyclerViewItemMode =
                            BucketRecyclerViewItemMode.GridStyle
                    } else
                        falleryOptions.bucketRecyclerViewItemMode =
                            BucketRecyclerViewItemMode.LinearStyle

                    it.icon =
                        getRecyclerViewItemViewModeIcon(falleryOptions.bucketRecyclerViewItemMode)
                    falleryViewModel.changeRecyclerViewItemMode(falleryOptions.bucketRecyclerViewItemMode)

                }

                R.id.bucketListMenuItemCamera -> takePhoto()
            }

            true
        }

        binding.floatingButtonSendMedia.setOnClickListener {
            falleryViewModel.prepareSelectedResults()
        }
    }

    private fun addCameraMenuItem() {
        binding.toolbarFalleryActivity.apply {
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
                File(
                    FalleryActivityComponentHolder.getOrNull()!!.provideCacheDir().cacheDir,
                    filename
                )
            }).also { temporaryFile ->
                getIntentForTakingPhoto(
                    it.fileProviderAuthority!!,
                    temporaryFile
                )?.also { takePhotoIntent ->
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
        val tintColor = FalleryActivityComponentHolder.getOrNull()
            ?.provideFalleryStyleAttrs()?.falleryToolbarIconTintColor ?: Color.BLACK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(
                tintColor,
                BlendMode.SRC_IN
            )
        } else
            drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
    }

    private fun addRecyclerViewItemViewModeMenuItem() {
        binding.toolbarFalleryActivity.apply {
            falleryOptions.bucketItemModeToggleEnabled.also {
                val mode = falleryOptions.bucketRecyclerViewItemMode
                if (it) {
                    menu.add(
                        0,
                        R.id.bucketListMenuItemShowRecyclerViewItemModelChanger,
                        0,
                        R.string.list_mode
                    ).apply {
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
            binding.toolbarFalleryActivity.menu?.findItem(R.id.bucketListMenuItemShowRecyclerViewItemModelChanger)?.isVisible =
                supportFragmentManager.findFragmentById(R.id.layoutFragmentContainer) is BucketListFragment
        } catch (ignored: Throwable) {
            ignored.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && requestCode == WRITE_EXTERNAL_REQUEST_CODE && grantResults.firstOrNull { it != PackageManager.PERMISSION_GRANTED } == null) {
            initialize()
        } else if (grantResults.isNotEmpty() && requestCode == WRITE_EXTERNAL_REQUEST_CODE) {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES
            else
                Manifest.permission.WRITE_EXTERNAL_STORAGE

            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this@FalleryActivity,
                    permission,
                )
            ) {
                showPermanentlyPermissionDeniedDialog()
            } else {
                writeExternalStoragePermissionDenied()
            }
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
                    falleryViewModel.shouldInitializeAfterStart = true
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
        if (captionBinding.relativeLayoutCaptionLayout.visibility == View.GONE) return
        binding.viewStubCaptionLayout.findViewById<EditText>(R.id.falleryEditTextCaption)
            ?.hideKeyboard()
        if (!withAnim)
            captionBinding.relativeLayoutCaptionLayout.visibility = View.GONE
        else {
            captionBinding.relativeLayoutCaptionLayout.startAnimation(
                TranslateAnimation(
                    0f, 0f, 0f, (captionBinding.relativeLayoutCaptionLayout.height).toFloat()
                ).apply {
                    fillAfter = true
                    duration = 250
                    setOnAnimationEndListener {
                        captionBinding.relativeLayoutCaptionLayout.visibility = View.GONE
                        captionBinding.relativeLayoutCaptionLayout.animation = null
                    }
                }
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun showCaptionLayout(withAnim: Boolean) {
        prepareCaptionViewStub()
        if (captionBinding.relativeLayoutCaptionLayout.visibility == View.VISIBLE) return
        if (!withAnim)
            captionBinding.relativeLayoutCaptionLayout.visibility = View.VISIBLE
        else {
            captionBinding.relativeLayoutCaptionLayout.visibility = View.VISIBLE
            captionBinding.relativeLayoutCaptionLayout.startAnimation(
                TranslateAnimation(
                    0f, 0f, (captionBinding.relativeLayoutCaptionLayout.height).toFloat(), 0f
                ).apply {
                    fillAfter = true
                    duration = 250
                    setOnAnimationEndListener {
                        captionBinding.relativeLayoutCaptionLayout.animation = null
                    }
                }
            )
        }
    }

    private fun prepareCaptionViewStub() {
        if (binding.viewStubCaptionLayout.parent != null) {
            (try {
                binding.viewStubCaptionLayout.inflate()
                captionBinding.imageViewSendMessage.setOnClickListener { falleryViewModel.prepareSelectedResults() }
                falleryOptions.captionEnabledOptions.editTextLayoutResId.let {
                    LayoutInflater.from(this)
                        .inflate(it, captionBinding.frameLayoutCaptionHolder, false)
                        .findViewById<AppCompatEditText>(R.id.falleryEditTextCaption).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        }
                }
            } catch (ignored: Throwable) {
                Log.e(
                    FALLERY_LOG_TAG,
                    "error while inflating captionLayoutResId. switch to default implementation"
                )
                LayoutInflater.from(this).inflate(
                    R.layout.caption_edit_text_layout,
                    captionBinding.frameLayoutCaptionHolder,
                    false
                )
                    .findViewById(R.id.falleryEditTextCaption)
            }).also {
                captionBinding.frameLayoutCaptionHolder.addView(it)
            }
        }

    }

    override fun onDestroy() {
        FalleryActivityComponentHolder.onDestroy()
        if (isFinishing) {
            FalleryCoreComponentHolder.onDestroy()
        }
        super.onDestroy()
    }

    private fun showSendButton(withAnim: Boolean = true) {
        if (binding.frameLayoutSendMedia.visibility == View.VISIBLE) return

        if (!withAnim) {
            binding.frameLayoutSendMedia.visibility = View.GONE
            return
        }

        binding.floatingButtonSendMedia.visibility = View.INVISIBLE
        binding.frameLayoutSendMedia.visibility = View.INVISIBLE
        frameLayoutSendMediaAnimationPostRunnable = Runnable {
            binding.floatingButtonSendMedia.visibility = View.VISIBLE
            binding.floatingButtonSendMedia.startAnimation(
                TranslateAnimation(
                    (binding.floatingButtonSendMedia.height).toFloat(), 0f, 0f, 0f
                ).apply {
                    fillAfter = true
                    duration = 200
                    setOnAnimationEndListener {
                        binding.floatingButtonSendMedia.animation = null
                    }
                }
            )

            binding.frameLayoutSendMedia.visibility = View.VISIBLE
            binding.frameLayoutSendMedia.startAnimation(
                TranslateAnimation(
                    0f, 0f, (binding.frameLayoutSendMedia.height).toFloat(), 0f
                ).apply {
                    fillAfter = true
                    duration = 200
                    setOnAnimationEndListener {
                        binding.frameLayoutSendMedia.animation = null
                    }
                }
            )
        }

        frameLayoutSendMediaAnimationPostRunnable?.also(binding.frameLayoutSendMedia::post)
    }

    private fun hideSendButton(withAnim: Boolean = true) {
        if (frameLayoutSendMediaAnimationPostRunnable != null) {
            binding.frameLayoutSendMedia.removeCallbacks(frameLayoutSendMediaAnimationPostRunnable)
            frameLayoutSendMediaAnimationPostRunnable = null
        }

        if (binding.frameLayoutSendMedia.visibility == View.GONE) return

        if (!withAnim) {
            binding.frameLayoutSendMedia.visibility = View.VISIBLE
            return
        }

        binding.floatingButtonSendMedia.startAnimation(
            TranslateAnimation(
                0f, (binding.floatingButtonSendMedia.height).toFloat(), 0f, 0f
            ).apply {
                fillAfter = true
                duration = 200
                setOnAnimationEndListener {
                    binding.floatingButtonSendMedia.visibility = View.GONE
                    binding.floatingButtonSendMedia.animation = null
                }
            }
        )

        binding.frameLayoutSendMedia.startAnimation(
            TranslateAnimation(
                0f, 0f, 0f, (binding.frameLayoutSendMedia.height).toFloat()
            ).apply {
                fillAfter = true
                duration = 200
                setOnAnimationEndListener {
                    binding.frameLayoutSendMedia.visibility = View.GONE
                    binding.frameLayoutSendMedia.animation = null
                }
            }
        )
    }


    override fun showToolbar(withAnim: Boolean) {
        if (binding.toolbarFalleryActivity.visibility == View.VISIBLE) return
        fun setHeightOfFragmentContainer() {
            binding.layoutFragmentContainer.layoutParams =
                (binding.layoutFragmentContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                    height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                }
        }
        if (!withAnim) {
            setHeightOfFragmentContainer()
            binding.toolbarFalleryActivity.visibility = View.VISIBLE
            return
        }

        binding.toolbarFalleryActivity.startAnimation(
            TranslateAnimation(
                0f,
                0f,
                -binding.toolbarFalleryActivity.height.toFloat(),
                0f
            ).apply {
                duration = 200
                fillAfter = true
                setOnAnimationEndListener {
                    setHeightOfFragmentContainer()
                    binding.toolbarFalleryActivity.visibility = View.VISIBLE
                    binding.toolbarFalleryActivity.animation = null
                }
            })
    }

    override fun hideToolbar(withAnim: Boolean) {
        if (binding.toolbarFalleryActivity.visibility == View.GONE) return
        fun setHeightOfFragmentContainer() {
            binding.layoutFragmentContainer.layoutParams =
                (binding.layoutFragmentContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                    height = ConstraintLayout.LayoutParams.MATCH_PARENT
                }
        }
        if (!withAnim) {
            setHeightOfFragmentContainer()
            binding.toolbarFalleryActivity.visibility = View.GONE
            return
        }

        binding.toolbarFalleryActivity.startAnimation(
            TranslateAnimation(
                0f,
                0f,
                0f,
                -binding.toolbarFalleryActivity.height.toFloat()
            ).apply {
                duration = 200
                fillAfter = true
                setOnAnimationEndListener {
                    setHeightOfFragmentContainer()
                    binding.toolbarFalleryActivity.visibility = View.GONE
                    binding.toolbarFalleryActivity.animation = null
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
                binding.toolbarFalleryActivity.title = getString(falleryOptions.toolbarTitle)
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

    override fun onStop() {
        frameLayoutSendMediaAnimationPostRunnable?.also {
            binding.frameLayoutSendMedia.removeCallbacks(it)
        }
        super.onStop()
    }

    private fun finishWithOKResult(it: Array<String>) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(FALLERY_MEDIAS_LIST_KEY, it)
            if (falleryOptions.captionEnabledOptions.enabled) {
                putExtra(
                    FALLERY_CAPTION_KEY,
                    captionBinding.frameLayoutCaptionHolder.findViewById<EditText>(R.id.falleryEditTextCaption)?.text?.toString()
                        ?.trim()
                )
            }
        })
        finish()
    }
}