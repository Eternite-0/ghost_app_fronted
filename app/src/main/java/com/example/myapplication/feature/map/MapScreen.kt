package com.example.myapplication.feature.map

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.amap.api.maps.AMap
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.example.myapplication.core.model.StoryMapMarker
import com.example.myapplication.core.model.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var mapLoaded by remember { mutableStateOf(false) }
    var showDiagnostics by remember { mutableStateOf(false) }
    val defaultLat = 30.0
    val defaultLon = 120.0

    LaunchedEffect(Unit) {
        viewModel.loadStories(defaultLat, defaultLon)
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(6000)
        if (!mapLoaded) {
            showDiagnostics = true
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Map") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { viewModel.loadStories(defaultLat, defaultLon) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSearch,
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToCreate,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Create") },
                    label = { Text("Create") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToNotifications,
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                    label = { Text("Alerts") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { Text("Map load failed") }
            }

            is UiState.Success -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AMapView(
                        modifier = Modifier.fillMaxSize(),
                        markers = state.data,
                        defaultLat = defaultLat,
                        defaultLon = defaultLon,
                        onMapLoaded = {
                            mapLoaded = true
                            showDiagnostics = false
                        }
                    )
                    if (showDiagnostics) {
                        Text(
                            text = "Map tiles not loaded. Check AMap SHA1+Package, emulator graphics, or use a real device.",
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AMapView(
    modifier: Modifier,
    markers: List<StoryMapMarker>,
    defaultLat: Double,
    defaultLon: Double,
    onMapLoaded: () -> Unit
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember {
        MapsInitializer.updatePrivacyShow(appContext, true, true)
        MapsInitializer.updatePrivacyAgree(appContext, true)
        TextureMapView(context).also {
            it.onCreate(Bundle())
            it.onResume()
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = {
            val aMap: AMap = it.map
            aMap.setOnMapLoadedListener { onMapLoaded() }
            aMap.mapType = AMap.MAP_TYPE_NORMAL
            aMap.clear()
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(defaultLat, defaultLon), 11f))
            markers.forEach { marker ->
                val lat = marker.location.latitude
                val lon = marker.location.longitude
                aMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lon))
                        .title(marker.title)
                        .snippet("${marker.category} | likes ${marker.likesCount}")
                )
            }
        }
    )
}
