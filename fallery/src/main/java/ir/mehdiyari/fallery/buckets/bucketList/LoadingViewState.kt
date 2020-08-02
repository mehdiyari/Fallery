package ir.mehdiyari.fallery.buckets.bucketList

internal sealed class LoadingViewState {
    object ShowLoading : LoadingViewState()
    object HideLoading : LoadingViewState()
    object Error : LoadingViewState()
}