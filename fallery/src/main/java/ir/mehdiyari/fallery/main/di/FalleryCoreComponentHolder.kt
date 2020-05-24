package ir.mehdiyari.fallery.main.di

import ir.mehdiyari.fallery.main.fallery.FalleryOptions
import ir.mehdiyari.fallery.main.di.component.FalleryCoreComponent
import ir.mehdiyari.fallery.main.di.component.FalleryCoreComponentBuilder

internal object FalleryCoreComponentHolder {

    private var falleryCoreComponent: FalleryCoreComponent? = null

    fun createComponent(falleryOptions: FalleryOptions) {
        if (falleryCoreComponent == null)
            falleryCoreComponent = FalleryCoreComponentBuilder()
                .bindFalleryOptions(falleryOptions).build()
    }

    fun getOrThrow(): FalleryCoreComponent {
        require(falleryCoreComponent != null) {
            "falleryCoreComponent can't be null. please just use Fallery.startFalleryInActivity or Fallery.startFalleryInFragment for starting fallery"
        }
        return falleryCoreComponent!!
    }

    /**
     * this method must be called when fallery views(all) destroyed
     */
    fun onDestroy() {
        falleryCoreComponent?.releaseCoreComponent()
        falleryCoreComponent = null
    }
}