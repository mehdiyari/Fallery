package ir.mehdiyari.falleryExample.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.mehdiyari.falleryExample.R
import kotlinx.android.synthetic.main.navigation_view.*

class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {

    var selectedItemId: Int = R.id.menuDefaultOptions

    var onMenuItemSelected: ((itemId: Int) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.navigation_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationViewExample.menu.findItem(selectedItemId).isChecked = true
        navigationViewExample.setNavigationItemSelectedListener {
            handleOnItemClick(it)
            true
        }
    }

    private fun handleOnItemClick(menuItem: MenuItem) {
        navigationViewExample.menu.forEach {
            if (it.isChecked) {
                it.isChecked = false
                return@forEach
            }
        }

        menuItem.isChecked = true
        Handler().postDelayed({
            onMenuItemSelected?.invoke(menuItem.itemId)
            dismiss()
        }, 400)
    }
}