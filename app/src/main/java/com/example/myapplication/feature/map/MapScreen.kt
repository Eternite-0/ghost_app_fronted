package com.example.myapplication.feature.map

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.core.location.LocationHelper
import com.example.myapplication.core.model.StoryMapMarker
import com.example.myapplication.core.model.UiState
import kotlinx.coroutines.launch
import kotlin.math.floor
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.MapTileIndex
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable

private enum class MapMode(val label: String) {
    Nearby("附近"),
    China("全国"),
    Global("全球")
}

private val GLOBAL_STREET_TILE_SOURCE = object : OnlineTileSourceBase(
    "ArcGISStreetSingleHost",
    0,
    19,
    256,
    "",
    arrayOf("https://server.arcgisonline.com"),
    "ArcGIS World Street"
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val z = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/$z/$y/$x"
    }
}

private val CHINA_CN_TILE_SOURCE = object : OnlineTileSourceBase(
    "GaodeCnStreet",
    3,
    18,
    256,
    ".png",
    arrayOf(
        "https://webrd01.is.autonavi.com",
        "https://webrd02.is.autonavi.com",
        "https://webrd03.is.autonavi.com",
        "https://webrd04.is.autonavi.com"
    ),
    "Gaode Chinese Street"
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val z = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        val s = (x + y) and 3
        return "https://webrd0${s + 1}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x=$x&y=$y&z=$z"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var mode by remember { mutableStateOf(MapMode.Nearby) }
    var locationStatus by remember { mutableStateOf("默认中心：华东区域") }
    var center by remember { mutableStateOf(GeoPoint(30.0, 120.0)) }
    var zoom by remember { mutableStateOf(5.5) }

    suspend fun loadNearby() {
        val loc = LocationHelper.getCurrentLocation(context)
        if (loc != null) {
            center = GeoPoint(loc.latitude, loc.longitude)
            zoom = 8.0
            locationStatus = "设备定位：%.3f, %.3f".format(loc.latitude, loc.longitude)
        } else {
            center = GeoPoint(30.0, 120.0)
            zoom = 6.0
            locationStatus = "未取到定位，使用默认中心"
        }
        viewModel.loadStories(center.latitude, center.longitude, 500_000)
    }

    fun loadByMode(selected: MapMode) {
        when (selected) {
            MapMode.Nearby -> scope.launch { loadNearby() }
            MapMode.China -> {
                center = GeoPoint(35.0, 103.0)
                zoom = 3.8
                locationStatus = "全国模式"
                viewModel.loadStories(35.0, 103.0, 6_500_000)
            }
            MapMode.Global -> {
                center = GeoPoint(20.0, 0.0)
                zoom = 2.0
                locationStatus = "全球模式"
                viewModel.loadStories(0.0, 0.0, 20_000_000)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) loadByMode(mode) else loadByMode(MapMode.China)
    }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            loadByMode(mode)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Map") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = { loadByMode(mode) }, icon = {
                    Icon(Icons.Default.Home, contentDescription = "Home")
                }, label = { Text("Home") })
                NavigationBarItem(selected = false, onClick = onNavigateToSearch, icon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }, label = { Text("Search") })
                NavigationBarItem(selected = false, onClick = onNavigateToCreate, icon = {
                    Icon(Icons.Default.Add, contentDescription = "Create")
                }, label = { Text("Create") })
                NavigationBarItem(selected = false, onClick = onNavigateToNotifications, icon = {
                    Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                }, label = { Text("Alerts") })
                NavigationBarItem(selected = false, onClick = onNavigateToProfile, icon = {
                    Icon(Icons.Default.Person, contentDescription = "Profile")
                }, label = { Text("Profile") })
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(locationStatus)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MapMode.entries.forEach { item ->
                    if (item == mode) {
                        Button(onClick = {}) { Text(item.label) }
                    } else {
                        OutlinedButton(onClick = {
                            mode = item
                            loadByMode(item)
                        }) { Text(item.label) }
                    }
                }
                OutlinedButton(onClick = { loadByMode(mode) }) { Text("刷新") }
            }

            when (val state = uiState) {
                is UiState.Loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is UiState.Error -> {
                    Text("地图数据加载失败")
                    OutlinedButton(onClick = { loadByMode(mode) }) { Text("重试") }
                }

                is UiState.Success -> {
                    RealMapView(
                        markers = state.data,
                        mode = mode,
                        center = center,
                        zoom = zoom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                    )
                    Text("故事列表（约 ${state.data.size} 条）")
                    if (state.data.isEmpty()) {
                        Text("暂无可展示坐标的故事")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(state.data.take(30)) { marker ->
                                val lat = marker.location.latitude
                                val lon = marker.location.longitude
                                Text("${marker.title} · ${lat?.let { "%.2f".format(it) }}, ${lon?.let { "%.2f".format(it) }}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RealMapView(
    markers: List<StoryMapMarker>,
    mode: MapMode,
    center: GeoPoint,
    zoom: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Configuration.getInstance().load(
        context,
        context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    )
    Configuration.getInstance().userAgentValue = "${context.packageName}.ghoststory"
    val lastClusterSignature = remember { mutableStateOf(0) }
    val mapView = remember {
        MapView(context).apply {
            setTileSource(GLOBAL_STREET_TILE_SOURCE)
            setUseDataConnection(true)
            isTilesScaledToDpi = false
            setMultiTouchControls(true)
            setFlingEnabled(true)
            minZoomLevel = 2.0
            maxZoomLevel = 18.0
        }
    }

    AndroidView(
        modifier = modifier.clipToBounds(),
        factory = { mapView },
        update = { mapView ->
            val targetTileSource = if (mode == MapMode.China) CHINA_CN_TILE_SOURCE else GLOBAL_STREET_TILE_SOURCE
            if (mapView.tileProvider.tileSource.name() != targetTileSource.name()) {
                mapView.setTileSource(targetTileSource)
                mapView.tileProvider.clearTileCache()
            }

            if (mode == MapMode.China) {
                // China mainland bounds only.
                mapView.setScrollableAreaLimitDouble(BoundingBox(54.0, 135.0, 18.0, 73.0))
                mapView.minZoomLevel = 3.5
                mapView.maxZoomLevel = 12.0
            } else {
                mapView.resetScrollableAreaLimitLatitude()
                mapView.resetScrollableAreaLimitLongitude()
                mapView.minZoomLevel = 2.0
                mapView.maxZoomLevel = 18.0
            }
            mapView.controller.setZoom(zoom)
            mapView.controller.setCenter(center)
            val currentZoom = mapView.zoomLevelDouble
            val clusters = clusterMarkers(markers, currentZoom)
            val signature = clusters.fold(17) { acc, c ->
                31 * acc + c.count + (c.lat * 10).toInt() + (c.lon * 10).toInt()
            }
            if (signature != lastClusterSignature.value) {
                lastClusterSignature.value = signature
                mapView.overlays.removeAll { it is Marker }
                clusters.forEach { cluster ->
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(cluster.lat, cluster.lon)
                        title = if (cluster.count > 1) "${cluster.count} stories" else cluster.titles.firstOrNull() ?: "Story"
                        snippet = if (cluster.count > 1) "Cluster" else cluster.category
                        icon = BitmapDrawable(
                            context.resources,
                            if (cluster.count > 1) createClusterBitmap(cluster.count) else createSinglePointBitmap()
                        )
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    mapView.overlays.add(marker)
                }
            }
            mapView.invalidate()
        }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }
}

private data class MarkerCluster(
    val lat: Double,
    val lon: Double,
    val count: Int,
    val titles: List<String>,
    val category: String
)

private fun clusterMarkers(markers: List<StoryMapMarker>, zoom: Double): List<MarkerCluster> {
    val cell = when {
        zoom < 3.0 -> 8.0
        zoom < 4.0 -> 4.0
        zoom < 5.0 -> 2.0
        zoom < 6.0 -> 1.0
        zoom < 7.0 -> 0.5
        zoom < 8.0 -> 0.25
        else -> 0.08
    }

    val grouped = linkedMapOf<Pair<Int, Int>, MutableList<StoryMapMarker>>()
    markers.forEach { marker ->
        val lat = marker.location.latitude ?: return@forEach
        val lon = marker.location.longitude ?: return@forEach
        val key = Pair(
            floor((lat + 90.0) / cell).toInt(),
            floor((lon + 180.0) / cell).toInt()
        )
        grouped.getOrPut(key) { mutableListOf() }.add(marker)
    }

    return grouped.values.map { list ->
        val valid = list.mapNotNull { m ->
            val lat = m.location.latitude ?: return@mapNotNull null
            val lon = m.location.longitude ?: return@mapNotNull null
            Pair(lat, lon)
        }
        val avgLat = valid.map { it.first }.average()
        val avgLon = valid.map { it.second }.average()
        MarkerCluster(
            lat = avgLat,
            lon = avgLon,
            count = list.size,
            titles = list.map { it.title }.take(6),
            category = list.firstOrNull()?.category ?: ""
        )
    }
}

private fun createSinglePointBitmap(): Bitmap {
    val size = 36
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#E63A32") }
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    val r = size / 2f - 2f
    canvas.drawCircle(size / 2f, size / 2f, r, fill)
    canvas.drawCircle(size / 2f, size / 2f, r, stroke)
    return bitmap
}

private fun createClusterBitmap(count: Int): Bitmap {
    val size = if (count >= 100) 86 else 74
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val outer = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#F59D99") }
    val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#D7261D") }
    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        textAlign = Paint.Align.CENTER
        textSize = if (count >= 100) 28f else 30f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val c = size / 2f
    canvas.drawCircle(c, c, size * 0.47f, outer)
    canvas.drawCircle(c, c, size * 0.36f, inner)
    val y = c - (text.descent() + text.ascent()) / 2f
    canvas.drawText(count.toString(), c, y, text)
    return bitmap
}
