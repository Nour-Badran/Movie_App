package com.example.movieapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import androidx.hilt.navigation.compose.hiltViewModel // Required import for hiltViewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.movieapp.model.Movie
import com.example.movieapp.model.Resource
import com.example.movieapp.ui.theme.MovieAppTheme
import com.example.movieapp.viewmodel.MovieViewModel

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

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object MovieDetails : Screen("movie_details/{movieId}") {
        fun createRoute(movieId: Int) = "movie_details/$movieId"
    }
}

@Composable
fun MovieNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }
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
fun SplashScreen(navController: NavHostController) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash_animation))
    val progress by animateLottieCompositionAsState(composition, iterations = 1) // Play animation once

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = progress,
            modifier = Modifier.fillMaxWidth().fillMaxHeight()
        )
    }

    // Navigate to HomeScreen when animation ends and pop the Splash screen from the stack
    LaunchedEffect(progress) {
        if (progress == 1f) { // Animation completed
            navController.navigate(Screen.Home.route) {
                // Ensure the splash screen is not in the back stack
                popUpTo("splash") { inclusive = true }
            }
        }
    }
}


@Composable
fun HomeScreen(navController: NavHostController, viewModel: MovieViewModel = hiltViewModel()) {
    val popularMovies = viewModel.popularMovies.observeAsState(Resource.Loading())
    val nowPlayingMovies = viewModel.nowPlayingMovies.observeAsState(Resource.Loading())
    val upcomingMovies = viewModel.upcomingMovies.observeAsState(Resource.Loading())

    LaunchedEffect(Unit) {
        viewModel.fetchMovies("popular", "08f0a058bffe4fef71999d9f345f5a08")
        viewModel.fetchMovies("now_playing", "08f0a058bffe4fef71999d9f345f5a08")
        viewModel.fetchMovies("upcoming", "08f0a058bffe4fef71999d9f345f5a08")
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Now Playing", "Popular", "Upcoming")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.DarkGray, Color(0xFF1C1C1C))
                )
            )
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Discover Movies",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .shadow(8.dp)
        )

        // Modernized Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color.Transparent
                )
            },
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (selectedTabIndex == index) Color.White else Color.Gray
                            )
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedTabIndex == index) Color.Gray.copy(alpha = 0.3f) else Color.Transparent)
                        .padding(12.dp)
                )
            }
        }

        // Movie Content
        when (selectedTabIndex) {
            0 -> MovieList(moviesResource = nowPlayingMovies.value, navController)
            1 -> MovieList(moviesResource = popularMovies.value, navController)
            2 -> MovieList(moviesResource = upcomingMovies.value, navController)
        }
    }
}

@Composable
fun MovieList(moviesResource: Resource<List<Movie>>, navController: NavHostController) {
    when (moviesResource) {
        is Resource.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        }
        is Resource.Success -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(moviesResource.data ?: emptyList()) { movie ->
                    MovieItem(movie, navController)
                }
            }
        }
        is Resource.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${moviesResource.message}",
                    color = colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun MovieItem(movie: Movie, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(Screen.MovieDetails.createRoute(movie.id)) }
            .padding(8.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Movie Poster with Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2 / 3f)
            ) {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                    contentDescription = "${movie.title} poster",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient Overlay for Text Readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black),
                                startY = 0.5f
                            )
                        )
                )
            }

            // Text Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Release: ${movie.release_date}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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

    // Trigger the fetch operation when the screen is loaded
    LaunchedEffect(movieId) {
        viewModel.fetchMovieDetails(movieId = movieId, apiKey = "08f0a058bffe4fef71999d9f345f5a08")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Color.Black, Color(0x80000000))))
    ) {
        when (val resource = movieDetailsResource.value) {
            is Resource.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(50.dp),
                    color = colorScheme.primary
                )
            }
            is Resource.Success -> {
                resource.data?.let { movie ->
                    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Movie Image
                        item {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w780${movie.poster_path}",
                                contentDescription = "${movie.title} Poster",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(screenHeight * 0.7f)
                                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                            )
                        }

                        // Movie Details
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = movie.title,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "Released on: ${movie.release_date}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xCCFFFFFF)),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "Genres: ${movie.genres.joinToString(", ") { it.name }}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xCCFFFFFF)),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Text(
                                    text = "Overview",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = movie.overview,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xAAFFFFFF),
                                        lineHeight = 24.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
            is Resource.Error -> {
                Text(
                    text = "Error: ${resource.message}",
                    color = colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            null -> {
                Text(
                    text = "No data available.",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }

        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 8.dp),
            shape = CircleShape,
            containerColor = Color.DarkGray,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }
    }
}