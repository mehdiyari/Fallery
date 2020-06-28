package ir.mehdiyari.fallery.utils

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

open class BaseViewModel : ViewModel() {

    protected val viewModelScope by lazy {
        object : CoroutineScope {
            override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main.immediate
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }

}