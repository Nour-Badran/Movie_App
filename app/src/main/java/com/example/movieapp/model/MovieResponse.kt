package com.example.movieapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class MovieResponse(
    val results: List<Movie>
)

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val id: Int,
    val title: String,
    val release_date: String,
    val poster_path: String,
    val category: String // Add this to differentiate popular/now_playing/upcoming
)

@Entity(tableName = "movie_details")
data class MovieDetailEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val genres: String, // Store as comma-separated
    val runtime: Int,
    val release_date: String,
    val poster_path: String
)

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val genres: List<Genre>,
    val runtime: Int,
    val release_date: String,
    val poster_path: String
)

data class Genre(val name: String)
