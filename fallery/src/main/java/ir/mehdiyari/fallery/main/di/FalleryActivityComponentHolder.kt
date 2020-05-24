package ir.mehdiyari.fallery.main.di

import androidx.fragment.app.FragmentActivity
import ir.mehdiyari.fallery.main.di.component.FalleryActivityComponent
import ir.mehdiyari.fallery.main.di.component.FalleryActivityComponentBuilder
import ir.mehdiyari.fallery.utils.AbstractFeatureComponentHolder

internal object FalleryActivityComponentHolder :
    AbstractFeatureComponentHolder<FalleryActivityComponent>() {

    override fun componentCreator(activity: FragmentActivity): FalleryActivityComponent {
        return FalleryActivityComponentBuilder().plusFalleryActivity(falleryActivity = activity)
            .plusFalleryCoreComponent(FalleryCoreComponentHolder.getOrThrow())
            .build()
    }

    override fun onDestroy() {
        this.getOrNull()?.releaseBucketListComponent()
        super.onDestroy()
    }
}