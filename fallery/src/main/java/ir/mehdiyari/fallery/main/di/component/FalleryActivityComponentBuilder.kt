package ir.mehdiyari.fallery.main.di.component

import androidx.fragment.app.FragmentActivity
import ir.mehdiyari.fallery.main.di.module.FalleryActivityModule


internal class FalleryActivityComponentBuilder {

    private var falleryActivity: FragmentActivity? = null
    private var falleryCoreComponent: FalleryCoreComponent? = null

    fun plusFalleryActivity(falleryActivity: FragmentActivity): FalleryActivityComponentBuilder =
        this.apply {
            this.falleryActivity = falleryActivity
        }

    fun plusFalleryCoreComponent(falleryCoreComponent: FalleryCoreComponent) : FalleryActivityComponentBuilder = this.apply {
        this.falleryCoreComponent = falleryCoreComponent
    }

    fun build(): FalleryActivityModule {
        require(falleryActivity != null) { "falleryActivity must set " }
        require(falleryCoreComponent != null) { "falleryCoreComponent must set " }
        return FalleryActivityModule(
            falleryActivity!!.applicationContext, falleryActivity!!, falleryCoreComponent!!
        )
    }
}