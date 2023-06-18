package ir.mehdiyari.fallery.buckets.bucketContent.content

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ir.mehdiyari.fallery.R
import java.util.concurrent.atomic.AtomicBoolean

internal class RecyclerViewTouchListener(
    private val recyclerView: RecyclerView,
    private val context: Context,
    private val onScaleCallback: (zoomIn: Boolean) -> Unit
) : View.OnTouchListener {

    val minSpanCount = 2F
    val maxSpanCount: Float =
        (context.resources.displayMetrics.widthPixels / (context.resources.getDimension(R.dimen.min_size_bucket_content_item) / 1.5)).toFloat()

    private var currentState: TouchMode = TouchMode.None
    private val scaleGestureDetector by lazy {
        ScaleGestureDetector(context, ZoomGestureListener())
    }

    private val recyclerViewTouchEventAtomicFlag = AtomicBoolean(true)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        if (recyclerViewTouchEventAtomicFlag.get()) {
            recyclerView.onTouchEvent(event)
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentState = TouchMode.DRAG
            }
            MotionEvent.ACTION_UP -> {
                currentState = TouchMode.None
            }
        }

        return true
    }

    enum class TouchMode {
        None,
        DRAG,
        ZOOM
    }

    inner class ZoomGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private val startPoint = PointF(0F, 0F)

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            recyclerViewTouchEventAtomicFlag.compareAndSet(true, false)
            currentState = TouchMode.ZOOM
            startPoint.x = detector.currentSpanX
            startPoint.y = detector.currentSpanY
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            recyclerViewTouchEventAtomicFlag.set(true)
            val end = PointF(detector.currentSpanX, detector.currentSpanY)
            if (end.x >= startPoint.x && end.y >= startPoint.y) {
                onScaleCallback(true)
            } else if (startPoint.x > end.x && startPoint.y > end.y) {
                onScaleCallback(false)
            }

            startPoint.x = 0F
            startPoint.y = 0F
            super.onScaleEnd(detector)
        }
    }
}