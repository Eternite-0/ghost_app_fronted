package com.example.myapplication.feature.story

import android.Manifest
import android.content.Context
import android.net.Uri
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
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.location.LocationHelper
import com.example.myapplication.core.model.StoryCategories
import com.example.myapplication.core.model.UiState
import kotlinx.coroutines.launch
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
                            latitude = latitude,
                            longitude = longitude,
                            address = selectedLocationOption.name,
                            placeName = if (locationSource == "设备定位") "设备附近" else selectedLocationOption.name,
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
