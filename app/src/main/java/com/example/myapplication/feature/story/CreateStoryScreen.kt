package com.example.myapplication.feature.story

import android.Manifest
import android.content.Context
import android.net.Uri
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.location.LocationHelper
import com.example.myapplication.core.model.StoryCategories
import com.example.myapplication.core.model.UiState
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

private data class ApproxLocationOption(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreateStoryScreen(
    viewModel: CreateStoryViewModel,
    onNavigateBack: () -> Unit,
    isMapStory: Boolean
) {
    val categoryOptions = StoryCategories.forumCategories
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categoryOptions.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val locationOptions = remember {
        listOf(
            ApproxLocationOption("华北（北京）", 39.9042, 116.4074),
            ApproxLocationOption("华东（上海）", 31.2304, 121.4737),
            ApproxLocationOption("华南（广州）", 23.1291, 113.2644),
            ApproxLocationOption("西南（成都）", 30.5728, 104.0668),
            ApproxLocationOption("中部（武汉）", 30.5928, 114.3055)
        )
    }
    var locationExpanded by remember { mutableStateOf(false) }
    var selectedLocationOption by remember { mutableStateOf(locationOptions[1]) }
    var latitude by remember { mutableStateOf(selectedLocationOption.latitude) }
    var longitude by remember { mutableStateOf(selectedLocationOption.longitude) }
    var locationSource by remember { mutableStateOf("手动粗定位：${selectedLocationOption.name}") }
    var locationError by remember { mutableStateOf<String?>(null) }
    var locating by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            viewModel.resetState()
            onNavigateBack()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    if (showMapPicker) {
        FullscreenLocationPicker(
            initialLatitude = latitude,
            initialLongitude = longitude,
            onCancel = { showMapPicker = false },
            onConfirm = { picked ->
                latitude = picked.latitude
                longitude = picked.longitude
                locationSource = "地图点选"
                locationError = null
                showMapPicker = false
            }
        )
        return
    }

    fun requestDeviceLocation() {
        locating = true
        locationError = null
        scope.launch {
            val location = LocationHelper.getCurrentLocation(context)
            locating = false
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                locationSource = "设备定位"
            } else {
                locationError = "未获取到设备定位，继续使用手动粗定位"
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            requestDeviceLocation()
        } else {
            locationError = "未授予定位权限，继续使用手动粗定位"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isMapStory) "Create Map Story" else "Create Forum Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { categoryExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Category: $category")
                }
                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            if (isMapStory) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { locationExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("粗定位区域: ${selectedLocationOption.name}")
                    }
                    DropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false }
                    ) {
                        locationOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = {
                                    selectedLocationOption = option
                                    latitude = option.latitude
                                    longitude = option.longitude
                                    locationSource = "手动粗定位：${option.name}"
                                    locationError = null
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        if (LocationHelper.hasLocationPermission(context)) {
                            requestDeviceLocation()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !locating
                ) {
                    Text(if (locating) "定位中..." else "使用设备定位（可选）")
                }

                OutlinedButton(
                    onClick = { showMapPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("地图点选定位（全屏）")
                }

                Text(
                    text = "当前位置来源：$locationSource\n坐标：${
                        String.format(
                            Locale.US,
                            "%.4f, %.4f",
                            latitude,
                            longitude
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )

                locationError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5
            )

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedImageUri != null) "Image Selected" else "Select Image")
            }

            if (uiState is UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        val imageFile = selectedImageUri?.let { uri -> getFileFromUri(context, uri) }
                        viewModel.createStory(
                            title = title,
                            content = content,
                            category = category,
                            latitude = if (isMapStory) latitude else 35.0,
                            longitude = if (isMapStory) longitude else 103.0,
                            address = if (isMapStory) {
                                when (locationSource) {
                                    "设备定位" -> "设备附近"
                                    "地图点选" -> "地图点选"
                                    else -> selectedLocationOption.name
                                }
                            } else {
                                null
                            },
                            placeName = if (isMapStory) {
                                when (locationSource) {
                                    "设备定位" -> "设备附近"
                                    "地图点选" -> "地图点选"
                                    else -> selectedLocationOption.name
                                }
                            } else {
                                null
                            },
                            image = imageFile,
                            mapStory = isMapStory
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isMapStory) "Submit Map Story" else "Publish Post")
                }
            }

            if (uiState is UiState.Error) {
                Text(
                    text = (uiState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullscreenLocationPicker(
    initialLatitude: Double,
    initialLongitude: Double,
    onCancel: () -> Unit,
    onConfirm: (GeoPoint) -> Unit
) {
    val context = LocalContext.current
    val markerRef = remember { mutableStateOf<Marker?>(null) }
    var lastPosition by remember { mutableStateOf(GeoPoint(initialLatitude, initialLongitude)) }
    var shouldRecenter by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = "${context.packageName}.ghoststory"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("地图选点") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { onConfirm(lastPosition) }) {
                        Text("确认")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        minZoomLevel = 2.0
                        maxZoomLevel = 19.0
                        controller.setZoom(9.0)
                        controller.setCenter(lastPosition)
                        setOnTouchListener { v, event ->
                            if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_MOVE) {
                                v.parent?.requestDisallowInterceptTouchEvent(true)
                            }
                            false
                        }

                        val marker = Marker(this).apply {
                            position = lastPosition
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "已选位置"
                            isDraggable = true
                            setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                                override fun onMarkerDragStart(marker: Marker) = Unit

                                override fun onMarkerDrag(marker: Marker) = Unit

                                override fun onMarkerDragEnd(marker: Marker) {
                                    lastPosition = marker.position
                                }
                            })
                        }
                        markerRef.value = marker
                        overlays.add(marker)

                        overlays.add(
                            MapEventsOverlay(
                                object : MapEventsReceiver {
                                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                        marker.position = p
                                        invalidate()
                                        lastPosition = p
                                        return true
                                    }

                                    override fun longPressHelper(p: GeoPoint): Boolean {
                                        marker.position = p
                                        invalidate()
                                        lastPosition = p
                                        return true
                                    }
                                }
                            )
                        )
                    }
                },
                update = { mapView ->
                    if (shouldRecenter) {
                        mapView.controller.setCenter(lastPosition)
                        shouldRecenter = false
                    }
                    markerRef.value?.position = lastPosition
                    mapView.invalidate()
                },
                onRelease = { mapView ->
                    mapView.onPause()
                    mapView.onDetach()
                }
            )

            Text(
                text = "单击/长按地图选点，或拖动标记后点击右上角确认。",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = String.format(Locale.US, "当前坐标：%.5f, %.5f", lastPosition.latitude, lastPosition.longitude),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("upload", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file
    } catch (_: Exception) {
        null
    }
}
