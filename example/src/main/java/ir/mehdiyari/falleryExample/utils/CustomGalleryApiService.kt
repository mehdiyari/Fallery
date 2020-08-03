package ir.mehdiyari.falleryExample.utils

import com.squareup.moshi.Moshi
import ir.mehdiyari.fallery.models.Media
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface CustomGalleryApiService {

    @GET("fallery/buckets.json")
    suspend fun getBucketList(): List<NetBucketModel>

    @GET("fallery/{name}")
    suspend fun getBucketsContentById(@Path("name") name: String): List<Media>

    companion object {
        fun create(): CustomGalleryApiService {
            val moshi = Moshi.Builder().add(MediaJsonAdapterFactory()).build()
            val moshiConverterFactory = MoshiConverterFactory
                .create(moshi)
            return Retrofit.Builder()
                .baseUrl("http://mehdiyari.ir/")
                .addConverterFactory(moshiConverterFactory)
                .build().create(CustomGalleryApiService::class.java)
        }
    }
}