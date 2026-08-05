package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface WordPressService {
    @GET("wp/v2/posts")
    suspend fun getPosts(
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int = 1,
        @Query("categories") categories: Int? = null
    ): List<WPPostResponse>
}
