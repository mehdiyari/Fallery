package ir.mehdiyari.falleryExample.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.mehdiyari.falleryExample.R
import ir.mehdiyari.falleryExample.databinding.NavigationViewBinding

class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {

    private var _binding: NavigationViewBinding? = null
    private val binding get() = _binding!!

    var selectedItemId: Int = R.id.menuDefaultOptions

    var onMenuItemSelected: ((itemId: Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = NavigationViewBinding.inflate(layoutInflater).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.navigationViewExample.menu.findItem(selectedItemId).isChecked = true
        binding.navigationViewExample.setNavigationItemSelectedListener {
            handleOnItemClick(it)
            true
        }
    }

    private fun handleOnItemClick(menuItem: MenuItem) {
        binding.navigationViewExample.menu.forEach {
            if (it.isChecked) {
                it.isChecked = false
                return@forEach
            }
        }

        menuItem.isChecked = true
        Handler(Looper.getMainLooper()).postDelayed({
            onMenuItemSelected?.invoke(menuItem.itemId)
            dismiss()
        }, 400)
    }

    override fun onDestroyView() {
        binding.navigationViewExample.setNavigationItemSelectedListener(null)
        _binding = null
        super.onDestroyView()
    }
}