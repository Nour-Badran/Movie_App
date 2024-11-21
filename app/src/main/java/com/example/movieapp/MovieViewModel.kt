package com.example.movieapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(private val repository: MovieRepository) : ViewModel() {

    private val _movies = MutableLiveData<Resource<List<Movie>>>()
    val movies: LiveData<Resource<List<Movie>>> = _movies

    private val _movieDetails = MutableLiveData<Resource<MovieDetail>>()
    val movieDetails: LiveData<Resource<MovieDetail>> = _movieDetails

    fun fetchMovies(category: String, apiKey: String) {
        viewModelScope.launch {
            _movies.value = Resource.Loading()
            _movies.value = repository.getMovies(category, apiKey)
        }
    }

    fun fetchMovieDetails(movieId: Int, apiKey: String) {
        viewModelScope.launch {
            _movieDetails.value = Resource.Loading()
            _movieDetails.value = repository.getMovieDetails(movieId, apiKey)
        }
    }
}

