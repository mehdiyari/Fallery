package ir.mehdiyari.fallery.buckets.ui.bucketList

import android.util.Log
import io.mockk.*
import ir.mehdiyari.fallery.buckets.bucketList.BucketListViewModel
import ir.mehdiyari.fallery.buckets.bucketList.LoadingViewState
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.models.MediaBucket
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BucketListViewModelTest {

    private val abstractMediaBucketProvider by lazy { mockk<AbstractMediaBucketProvider>() }

    @ExperimentalCoroutinesApi
    private val bucketViewModel by lazy {
        spyk(
            BucketListViewModel(
                abstractMediaBucketProvider,
                BucketType.VIDEO_PHOTO_BUCKETS,
                testCoroutineDispatcher
            )
        )
    }

    private val mockViewStateCollector by lazy { spyk<FlowCollector<LoadingViewState?>>() }
    private val bucketsMutableStateFlowCollector by lazy { spyk<FlowCollector<List<MediaBucket>>>() }
    private val allMediaCountChangedStateFlowCollector by lazy { spyk<FlowCollector<Int>>() }

    @ExperimentalCoroutinesApi
    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    @ExperimentalCoroutinesApi
    private val testCoroutineScope by lazy { TestCoroutineScope(testCoroutineDispatcher) }

    @ExperimentalCoroutinesApi
    @BeforeEach
    fun before() {
        Dispatchers.setMain(testCoroutineDispatcher)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
    }

    @InternalCoroutinesApi
    @ExperimentalCoroutinesApi
    @Test
    fun `Given refresh=true - when app has not access to external storage - then notify showLoading-hideLoading-ErrorInFetchingBuckets`() = runBlockingTest {
        every { bucketViewModel.bucketsStateFlow.value } returns generateBucketsList()
        testCoroutineScope.launch { bucketViewModel.loadingViewStateFlow.collect(mockViewStateCollector) }
        coEvery { abstractMediaBucketProvider.getMediaBuckets(any()) } throws SecurityException("cant access to external storage")
        bucketViewModel.getBuckets(refresh = false)
        coVerify(exactly = 1) { mockViewStateCollector.emit(LoadingViewState.ShowLoading) }
        coVerify(exactly = 1) { mockViewStateCollector.emit(LoadingViewState.HideLoading) }
        coVerify(exactly = 1) { mockViewStateCollector.emit(LoadingViewState.Error) }
    }

    @InternalCoroutinesApi
    @ExperimentalCoroutinesApi
    @Test
    fun `Given refresh=false - when bucket lists is empty - then get bucket lists`() = runBlockingTest {
        val generatedList = generateBucketsList()
        testCoroutineScope.launch { bucketViewModel.loadingViewStateFlow.collect(mockViewStateCollector) }
        testCoroutineScope.launch { bucketViewModel.bucketsStateFlow.collect(bucketsMutableStateFlowCollector) }
        testCoroutineScope.launch { bucketViewModel.allMediaCountChanged.collect(allMediaCountChangedStateFlowCollector) }
        coEvery { abstractMediaBucketProvider.getMediaBuckets(any()) } returns generatedList
        bucketViewModel.getBuckets(refresh = false)
        coVerify(exactly = 1) { mockViewStateCollector.emit(LoadingViewState.ShowLoading) }
        coVerify(exactly = 1) { mockViewStateCollector.emit(LoadingViewState.HideLoading) }
        coVerify(exactly = 1) { bucketsMutableStateFlowCollector.emit(generatedList) }
        coVerify(exactly = 1) { allMediaCountChangedStateFlowCollector.emit(generatedList[0].mediaCount) }
    }

    @ExperimentalCoroutinesApi
    @AfterEach
    fun after() {
        Dispatchers.resetMain()
    }

    private fun generateBucketsList(): List<MediaBucket> = mutableListOf<MediaBucket>().apply {
        repeat(20) {
            add(
                MediaBucket(it.toLong(), "storage/$it/emulated/x", "Batman Folder $it", "batman.jpg", (it + 1) * 100)
            )
        }

        add(0, this[0].copy(displayName = "All Medias", mediaCount = this.sumBy { it.mediaCount }))
    }
}