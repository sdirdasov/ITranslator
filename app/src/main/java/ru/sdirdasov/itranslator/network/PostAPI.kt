package ru.sdirdasov.itranslator.network

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query
import ru.sdirdasov.itranslator.post.PostTranslated

interface PostAPI {

    @POST("api/v1.5/tr.json/translate")
    fun getPosts(
        @Query("key") APIKey: String,
        @Query("text") textToTranslate: String,
        @Query("lang") lang: String
    ): Call<PostTranslated>
}
