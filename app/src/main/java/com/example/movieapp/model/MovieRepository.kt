package com.example.movieapp.model

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(private val apiService: MovieApiService) {

    suspend fun getMovies(category: String, apiKey: String): Resource<List<Movie>> {
        return try {
            val response = when (category) {
                "now_playing" -> apiService.getNowPlaying(apiKey)
                "popular" -> apiService.getPopular(apiKey)
                else -> apiService.getUpcoming(apiKey)
            }
            if (response.isSuccessful) {
                Resource.Success(response.body()?.results ?: emptyList())
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    suspend fun getMovieDetails(movieId: Int, apiKey: String): Resource<MovieDetail> {
        return try {
            val response = apiService.getMovieDetails(movieId, apiKey)
            if (response.isSuccessful) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }
}


sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
