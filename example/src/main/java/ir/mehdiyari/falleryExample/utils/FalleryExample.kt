package ir.mehdiyari.falleryExample.utils

import android.app.Application

class FalleryExample : Application() {

    companion object {
        var customGalleryApiService: CustomGalleryApiService? = null
            get() {
                if (field == null)
                    field = CustomGalleryApiService.create()
                return field
            }
    }

}