package com.example.movieapp.model

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val apiService: MovieApiService,
    private val movieDao: MovieDao,
    private val movieDetailDao: MovieDetailDao
) {
    suspend fun getMovies(category: String, apiKey: String): Resource<List<Movie>> {
        val cached = movieDao.getMoviesByCategory(category)
        if (cached.isNotEmpty()) return Resource.Success(cached)

        return try {
            val response = when (category) {
                "now_playing" -> apiService.getNowPlaying(apiKey)
                "popular" -> apiService.getPopular(apiKey)
                else -> apiService.getUpcoming(apiKey)
            }
            if (response.isSuccessful) {
                val movies = response.body()?.results?.map { it.copy(category = category) } ?: emptyList()
                movieDao.insertAll(movies)
                Resource.Success(movies)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error")
        }
    }

    suspend fun getMovieDetails(movieId: Int, apiKey: String): Resource<MovieDetail> {
        val cached = movieDetailDao.getMovieDetails(movieId)
        if (cached != null) {
            val genres = cached.genres.split(",").map { Genre(it) }
            return Resource.Success(
                MovieDetail(
                    id = cached.id,
                    title = cached.title,
                    overview = cached.overview,
                    genres = genres,
                    runtime = cached.runtime,
                    release_date = cached.release_date,
                    poster_path = cached.poster_path
                )
            )
        }

        return try {
            val response = apiService.getMovieDetails(movieId, apiKey)
            if (response.isSuccessful) {
                val detail = response.body()!!
                movieDetailDao.insertMovieDetails(
                    MovieDetailEntity(
                        id = detail.id,
                        title = detail.title,
                        overview = detail.overview,
                        genres = detail.genres.joinToString(",") { it.name },
                        runtime = detail.runtime,
                        release_date = detail.release_date,
                        poster_path = detail.poster_path
                    )
                )
                Resource.Success(detail)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error")
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
