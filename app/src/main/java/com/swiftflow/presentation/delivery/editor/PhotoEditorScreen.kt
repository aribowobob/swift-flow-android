package com.swiftflow.presentation.delivery.editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.swiftflow.BuildConfig
import com.swiftflow.domain.model.DrawPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    deliveryId: Int,
    photoId: Int,
    photoUrl: String,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: PhotoEditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingImage by remember { mutableStateOf(true) }
    var imageLoadError by remember { mutableStateOf<String?>(null) }

    // Load the image
    LaunchedEffect(photoUrl) {
        isLoadingImage = true
        imageLoadError = null

        try {
            val baseUrl = BuildConfig.API_BASE_URL.replace("/api/", "")
            val fullUrl = "$baseUrl$photoUrl"

            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(fullUrl)
                .allowHardware(false)
                .build()

            val result = withContext(Dispatchers.IO) {
                imageLoader.execute(request)
            }

            if (result is SuccessResult) {
                val drawable = result.drawable
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                originalBitmap = bitmap
            } else {
                imageLoadError = "Failed to load image"
            }
        } catch (e: Exception) {
            imageLoadError = e.message ?: "Failed to load image"
        } finally {
            isLoadingImage = false
        }
    }

    // Show error in snackbar
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Photo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Undo button
                    IconButton(
                        onClick = { viewModel.undoLastPath() },
                        enabled = state.paths.isNotEmpty() && !state.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Undo"
                        )
                    }

                    // Clear button
                    IconButton(
                        onClick = { viewModel.clearAllPaths() },
                        enabled = state.paths.isNotEmpty() && !state.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    }

                    // Save button
                    IconButton(
                        onClick = {
                            originalBitmap?.let { bitmap ->
                                // Create the final bitmap with drawings
                                val editedBitmap = createEditedBitmap(bitmap, state.paths)
                                viewModel.saveEditedPhoto(
                                    deliveryId = deliveryId,
                                    photoId = photoId,
                                    editedBitmap = editedBitmap,
                                    onSuccess = onSaveSuccess
                                )
                            }
                        },
                        enabled = state.paths.isNotEmpty() && !state.isSaving && originalBitmap != null
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(8.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Drawing canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.DarkGray)
            ) {
                when {
                    isLoadingImage -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    imageLoadError != null -> {
                        Text(
                            text = imageLoadError ?: "Error loading image",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    originalBitmap != null -> {
                        DrawingCanvas(
                            backgroundBitmap = originalBitmap!!,
                            paths = state.paths,
                            currentColor = state.currentColor,
                            strokeWidth = state.strokeWidth,
                            onPathDrawn = { path -> viewModel.addPath(path) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Saving overlay
                if (state.isSaving) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }

            // Color palette
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                ColorPalette(
                    selectedColor = state.currentColor,
                    onColorSelected = { color -> viewModel.setColor(color) }
                )
            }
        }
    }
}

@Composable
private fun DrawingCanvas(
    backgroundBitmap: Bitmap,
    paths: List<DrawPath>,
    currentColor: Color,
    strokeWidth: Float,
    onPathDrawn: (DrawPath) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var imageOffset by remember { mutableStateOf(IntOffset.Zero) }
    var imageDisplaySize by remember { mutableStateOf(IntSize.Zero) }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentPath = Path().apply {
                            moveTo(offset.x, offset.y)
                        }
                        currentPoints = listOf(offset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentPath?.lineTo(change.position.x, change.position.y)
                        currentPoints = currentPoints + change.position
                    },
                    onDragEnd = {
                        currentPath?.let { path ->
                            onPathDrawn(
                                DrawPath(
                                    path = path,
                                    points = currentPoints,
                                    color = currentColor,
                                    strokeWidth = strokeWidth
                                )
                            )
                        }
                        currentPath = null
                        currentPoints = emptyList()
                    },
                    onDragCancel = {
                        currentPath = null
                        currentPoints = emptyList()
                    }
                )
            }
    ) {
        canvasSize = IntSize(size.width.toInt(), size.height.toInt())

        // Calculate image scaling and position (fit center)
        val imageAspect = backgroundBitmap.width.toFloat() / backgroundBitmap.height.toFloat()
        val canvasAspect = size.width / size.height

        val (drawWidth, drawHeight) = if (imageAspect > canvasAspect) {
            // Image is wider - fit by width
            size.width to size.width / imageAspect
        } else {
            // Image is taller - fit by height
            size.height * imageAspect to size.height
        }

        val offsetX = (size.width - drawWidth) / 2
        val offsetY = (size.height - drawHeight) / 2

        imageOffset = IntOffset(offsetX.toInt(), offsetY.toInt())
        imageDisplaySize = IntSize(drawWidth.toInt(), drawHeight.toInt())

        // Draw background image
        drawImage(
            image = backgroundBitmap.asImageBitmap(),
            dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
            dstSize = IntSize(drawWidth.toInt(), drawHeight.toInt())
        )

        // Draw completed paths
        paths.forEach { drawPath ->
            drawPath(
                path = drawPath.path,
                color = drawPath.color,
                style = Stroke(
                    width = drawPath.strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        // Draw current path being drawn
        currentPath?.let { path ->
            drawPath(
                path = path,
                color = currentColor,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

private fun createEditedBitmap(
    originalBitmap: Bitmap,
    paths: List<DrawPath>
): Bitmap {
    // Create a mutable copy of the original bitmap
    val editedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = android.graphics.Canvas(editedBitmap)

    // Draw all paths on the bitmap
    val paint = android.graphics.Paint().apply {
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
        isAntiAlias = true
    }

    paths.forEach { drawPath ->
        paint.color = android.graphics.Color.argb(
            (drawPath.color.alpha * 255).toInt(),
            (drawPath.color.red * 255).toInt(),
            (drawPath.color.green * 255).toInt(),
            (drawPath.color.blue * 255).toInt()
        )
        paint.strokeWidth = drawPath.strokeWidth

        // Convert stored points to Android Path
        if (drawPath.points.isNotEmpty()) {
            val androidPath = android.graphics.Path()
            val firstPoint = drawPath.points.first()
            androidPath.moveTo(firstPoint.x, firstPoint.y)

            drawPath.points.drop(1).forEach { point ->
                androidPath.lineTo(point.x, point.y)
            }

            canvas.drawPath(androidPath, paint)
        }
    }

    return editedBitmap
}
