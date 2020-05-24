package ir.mehdiyari.fallery.main.di.component

import ir.mehdiyari.fallery.main.di.module.FalleryCoreModule
import ir.mehdiyari.fallery.main.fallery.FalleryOptions

internal class FalleryCoreComponentBuilder {

    private var falleryOptions: FalleryOptions? = null

    fun bindFalleryOptions(falleryOptions: FalleryOptions): FalleryCoreComponentBuilder {
        this.falleryOptions = falleryOptions
        return this
    }

    fun build(): FalleryCoreModule {
        require(falleryOptions != null) { "falleryOptions must not be null" }

        return FalleryCoreModule(falleryOptions!!)
    }
}