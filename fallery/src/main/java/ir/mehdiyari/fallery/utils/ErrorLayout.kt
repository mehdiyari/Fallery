package ir.mehdiyari.fallery.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import ir.mehdiyari.fallery.R

internal class ErrorLayout(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet) {

    init {
        View.inflate(context, R.layout.error_layout, this)
    }

    fun show() {
        findViewById<ConstraintLayout>(R.id.constraintLayoutRootLayout).visibility = View.VISIBLE
    }

    fun hide() {
        findViewById<ConstraintLayout>(R.id.constraintLayoutRootLayout).visibility = View.GONE
    }

    fun setOnRetryClickListener(function: () -> Unit) {
        findViewById<AppCompatTextView>(R.id.textViewRetry).setOnClickListener {
            function.invoke()
        }
    }

}