package ir.mehdiyari.fallery.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import ir.mehdiyari.fallery.R
import kotlinx.android.synthetic.main.error_layout.view.*

internal class ErrorLayout constructor(context: Context, attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {

    init {
        View.inflate(context, R.layout.error_layout, this)
    }

    fun show() {
        linearLayoutRootLayout.visibility = View.VISIBLE
    }

    fun hide() {
        linearLayoutRootLayout.visibility = View.GONE
    }

    fun setOnRetryClickListener(function: () -> Unit) {
        textViewRetry.setOnClickListener {
            function.invoke()
        }
    }

}