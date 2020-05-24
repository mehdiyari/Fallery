package ir.mehdiyari.fallery.models

import android.util.TypedValue
import androidx.fragment.app.FragmentActivity
import ir.mehdiyari.fallery.R

internal data class FalleryStyleAttrs(
    val falleryBackgroundColor: Int = 0,
    val falleryColorPrimary: Int = 0,
    val falleryColorPrimaryDark: Int = 0,
    val falleryColorAccent: Int = 0,
    val falleryTintColor: Int = 0,
    val falleryToolbarIconTintColor: Int = 0,
    val falleryPrimaryTextColor: Int = 0,
    val fallerySecondaryTextColor: Int = 0,
    val falleryHintTextColor: Int = 0,
    val falleryPlaceHolderColor: Int = 0
)

internal fun FragmentActivity.getFalleryStyleAttrs(): FalleryStyleAttrs = this.theme.let { activityTheme ->

    var falleryStyleAttrs = FalleryStyleAttrs()
    val typeValue = TypedValue()

    activityTheme.resolveAttribute(R.attr.fallery_background_color, typeValue, true)
    falleryStyleAttrs = falleryStyleAttrs.copy(falleryBackgroundColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.fallery_color_primary, typeValue, true)
    falleryStyleAttrs = falleryStyleAttrs.copy(falleryColorPrimary = typeValue.data)

    activityTheme.resolveAttribute(R.attr.fallery_color_primary_dark, typeValue, true)
    falleryStyleAttrs = falleryStyleAttrs.copy(falleryColorPrimaryDark = typeValue.data)

    activityTheme.resolveAttribute(R.attr.fallery_color_accent, typeValue, true)
    falleryStyleAttrs = falleryStyleAttrs.copy(falleryColorAccent = typeValue.data)

    activityTheme.resolveAttribute(R.attr.fallery_icon_tint_color, typeValue, true)
    falleryStyleAttrs = falleryStyleAttrs.copy(falleryTintColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.fallery_toolbar_icon_tint_color, typeValue, true)
    falleryStyleAttrs = falleryStyleAttrs.copy(falleryToolbarIconTintColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.fallery_primary_text_color, typeValue, true)
    falleryStyleAttrs = falleryStyleAttrs.copy(falleryPrimaryTextColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.fallery_secondary_text_color, typeValue, true)
    falleryStyleAttrs = falleryStyleAttrs.copy(fallerySecondaryTextColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.fallery_hint_text_color, typeValue, true)
    falleryStyleAttrs = falleryStyleAttrs.copy(falleryHintTextColor = typeValue.data)

    activityTheme.resolveAttribute(R.attr.fallery_place_holder_color, typeValue, true)
    falleryStyleAttrs = falleryStyleAttrs.copy(falleryPlaceHolderColor = typeValue.data)


    falleryStyleAttrs
}