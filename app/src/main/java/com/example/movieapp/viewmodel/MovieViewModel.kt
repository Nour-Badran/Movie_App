package com.example.movieapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.model.Movie
import com.example.movieapp.model.MovieDetail
import com.example.movieapp.model.MovieRepository
import com.example.movieapp.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(private val repository: MovieRepository) : ViewModel() {

    private val _popularMovies = MutableLiveData<Resource<List<Movie>>>()
    val popularMovies: LiveData<Resource<List<Movie>>> = _popularMovies

    private val _nowPlayingMovies = MutableLiveData<Resource<List<Movie>>>()
    val nowPlayingMovies: LiveData<Resource<List<Movie>>> = _nowPlayingMovies

    private val _upcomingMovies = MutableLiveData<Resource<List<Movie>>>()
    val upcomingMovies: LiveData<Resource<List<Movie>>> = _upcomingMovies

    private val _movieDetails = MutableLiveData<Resource<MovieDetail>>()
    val movieDetails: LiveData<Resource<MovieDetail>> = _movieDetails

    fun fetchMovies(category: String, apiKey: String) {
        viewModelScope.launch {
            when (category) {
                "popular" -> {
                    _popularMovies.value = Resource.Loading()
                    _popularMovies.value = repository.getMovies(category, apiKey)
                }
                "now_playing" -> {
                    _nowPlayingMovies.value = Resource.Loading()
                    _nowPlayingMovies.value = repository.getMovies(category, apiKey)
                }
                "upcoming" -> {
                    _upcomingMovies.value = Resource.Loading()
                    _upcomingMovies.value = repository.getMovies(category, apiKey)
                }
            }
        }
    }

    fun fetchMovieDetails(movieId: Int, apiKey: String) {
        viewModelScope.launch {
            _movieDetails.value = Resource.Loading()
            _movieDetails.value = repository.getMovieDetails(movieId, apiKey)
        }
    }
}


