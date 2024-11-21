package com.example.movieapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movieapp.ui.theme.MovieAppTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import androidx.hilt.navigation.compose.hiltViewModel // Required import for hiltViewModel

@HiltAndroidApp
class MovieApp : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    MovieNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding) // Apply innerPadding here
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MovieAppTheme {
        Greeting("Android")
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object MovieDetails : Screen("movie_details/{movieId}") {
        fun createRoute(movieId: Int) = "movie_details/$movieId"
    }
}

@Composable
fun MovieNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.MovieDetails.route) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull()
            if (movieId != null) {
                MovieDetailsScreen(movieId, navController)
            } else {
                // Handle invalid movieId case
                Text("Invalid movie ID")
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController, viewModel: MovieViewModel = hiltViewModel()) {
    val moviesResource = viewModel.movies.observeAsState(Resource.Loading())

    // Trigger the movie fetch when the screen is composed
    LaunchedEffect(true) {
        viewModel.fetchMovies(category = "popular", apiKey = "08f0a058bffe4fef71999d9f345f5a08")  // Replace with your API key
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Welcome to MovieApp!",
            style = androidx.compose.material.MaterialTheme.typography.h4
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Show loading state
        when (val resource = moviesResource.value) {
            is Resource.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is Resource.Success -> {
                val movieList = resource.data ?: emptyList()  // Ensure the list is non-null

                // Show the list of movies when data is successfully fetched
                LazyColumn {
                    items(movieList) { movie ->  // Iterate over each movie in the list
                        Text(
                            text = movie.title,  // Display the title of each movie
                            modifier = Modifier
                                .clickable {
                                    // Navigate to movie details screen using the individual movie's id
                                    navController.navigate(Screen.MovieDetails.createRoute(movie.id))
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
            is Resource.Error -> {
                // Handle error state
                Text(
                    text = "Error: ${resource.message}",
                    color = androidx.compose.material.MaterialTheme.colors.error
                )
            }
        }
    }
}


@Composable
fun MovieDetailsScreen(
    movieId: Int,
    navController: NavHostController,
    viewModel: MovieViewModel = hiltViewModel()
) {
    val movieDetailsResource = viewModel.movieDetails.observeAsState()

    // Fetch the movie details
    LaunchedEffect(movieId) {
        viewModel.fetchMovieDetails(movieId = movieId, apiKey = "08f0a058bffe4fef71999d9f345f5a08")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (val resource = movieDetailsResource.value) {
            is Resource.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is Resource.Success -> {
                // Display movie details
                val movieDetail = resource.data
                movieDetail?.let {
                    Text(text = "Title: ${it.title}")
                    Text(text = "Overview: ${it.overview}")
                    // Add more movie details here
                } ?: Text("No movie details available.")
            }
            is Resource.Error -> {
                Text(
                    text = "Error: ${resource.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            null -> {
                // Handle the null case explicitly
                Text("No data available.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Example button to navigate back to home screen
        Button(onClick = { navController.popBackStack() }) {
            Text("Back to Home")
        }
    }
}
