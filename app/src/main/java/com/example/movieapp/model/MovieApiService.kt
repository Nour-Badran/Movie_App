package com.example.movieapp.model

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {
    @GET("movie/now_playing")
    suspend fun getNowPlaying(@Query("api_key") apiKey: String): Response<MovieResponse>

    @GET("movie/popular")
    suspend fun getPopular(@Query("api_key") apiKey: String): Response<MovieResponse>

    @GET("movie/upcoming")
    suspend fun getUpcoming(@Query("api_key") apiKey: String): Response<MovieResponse>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Response<MovieDetail>
}
