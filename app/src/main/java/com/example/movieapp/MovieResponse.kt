package com.example.movieapp

data class MovieResponse(
    val results: List<Movie>
)

data class Movie(
    val id: Int,
    val title: String,
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
