package ir.mehdiyari.fallery.utils

import android.content.ContentResolver
import android.net.Uri
import android.os.Handler
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test


internal class MediaStoreObserverTest {

    private val handler by lazy { mockk<Handler>() }
    private val fragmentActivity by lazy { mockk<FragmentActivity>() }
    private val observer by lazy { spyk(MediaStoreObserver(handler, fragmentActivity)) }
    private val mockedLiveDataObserver by lazy { mockk<Observer<Uri>>() }
    private val lifecycleRegistry by lazy { mockk<LifecycleRegistry>() }
    private val contentResolver by lazy { mockk<ContentResolver>() }

    @Rule
    @JvmField
    val instantExecutionRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        every { fragmentActivity.lifecycle } returns lifecycleRegistry
        every { lifecycleRegistry.addObserver(any()) } returns Unit
        every { lifecycleRegistry.removeObserver(any()) } returns Unit
        every { fragmentActivity.contentResolver } returns contentResolver
        every { contentResolver.registerContentObserver(any(), any(), any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit
        every { mockedLiveDataObserver.onChanged(any()) } returns Unit
    }

    @Test
    fun testAddObserver() {
        observer.apply {
            verify(exactly = 1) { lifecycleRegistry.addObserver(any()) }
        }
    }

    @Test
    fun testRegisterListener() {
        observer.apply {
            verify(exactly = 4) { contentResolver.registerContentObserver(any(), any(), any()) }
        }
    }

    @Test
    fun testUnRegisterListener() {
        observer.apply {
            observer.onDestroy()
            verify(exactly = 1) { contentResolver.unregisterContentObserver(any()) }
        }
    }

    @Test
    fun testObserverChange() {
        observer.externalStorageChangeLiveData.observeForever(mockedLiveDataObserver)
        observer.onChange(true, Uri.EMPTY)
        verify(exactly = 1) { mockedLiveDataObserver.onChanged(Uri.EMPTY) }
    }
}