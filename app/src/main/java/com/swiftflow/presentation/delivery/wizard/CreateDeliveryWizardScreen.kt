package com.swiftflow.presentation.delivery.wizard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.swiftflow.domain.model.WizardStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDeliveryWizardScreen(
    onDeliveryCreated: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CreateDeliveryWizardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Use GetMultipleContents instead of PickMultipleVisualMedia
    // because Photo Picker strips EXIF location data for privacy
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            // Limit to MAX_PHOTOS
            viewModel.addPhotos(uris.take(CreateDeliveryWizardViewModel.MAX_PHOTOS))
        }
    }

    // Permission launcher for ACCESS_MEDIA_LOCATION
    val mediaLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Launch photo picker regardless of permission result
        // The permission helps with EXIF access but picker still works without it
        photoPickerLauncher.launch("image/*")
    }

    // Function to launch photo picker with permission check
    fun launchPhotoPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.ACCESS_MEDIA_LOCATION
            when {
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                    photoPickerLauncher.launch("image/*")
                }
                else -> {
                    mediaLocationPermissionLauncher.launch(permission)
                }
            }
        } else {
            photoPickerLauncher.launch("image/*")
        }
    }

    // Handle completion
    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            onDeliveryCreated()
        }
    }

    // Handle errors
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (state.currentStep) {
                            WizardStep.SelectPhotos -> "Create Delivery"
                            WizardStep.ReviewLocation -> "Review Location"
                            WizardStep.AddProducts -> "Add Products"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
            // Step indicator
            WizardStepIndicator(
                currentStep = state.currentStep,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Error display (if any)
            state.error?.let { error ->
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Step content
            when (state.currentStep) {
                WizardStep.SelectPhotos -> {
                    PhotoSelectionStep(
                        selectedPhotos = state.selectedPhotos,
                        isUploading = state.isUploading,
                        uploadProgress = state.uploadProgress,
                        totalPhotos = state.totalPhotos,
                        maxPhotos = CreateDeliveryWizardViewModel.MAX_PHOTOS,
                        onAddPhotos = { launchPhotoPicker() },
                        onRemovePhoto = { photo -> viewModel.removePhoto(photo) },
                        onNext = { viewModel.proceedToStep2() },
                        onCancel = onNavigateBack
                    )
                }

                WizardStep.ReviewLocation -> {
                    LocationReviewStep(
                        state = state,
                        onUpdateLocationName = { viewModel.updateLocationName(it) },
                        onUpdateStreet = { viewModel.updateStreet(it) },
                        onUpdateDistrict = { viewModel.updateDistrict(it) },
                        onUpdateCity = { viewModel.updateCity(it) },
                        onUpdateRegion = { viewModel.updateRegion(it) },
                        onUpdateLatitude = { viewModel.updateLatitude(it) },
                        onUpdateLongitude = { viewModel.updateLongitude(it) },
                        onNext = { viewModel.proceedToStep3() }
                    )
                }

                WizardStep.AddProducts -> {
                    ProductSelectionStep(
                        state = state,
                        onAddProduct = { product -> viewModel.addProduct(product) },
                        onRemoveProduct = { productId -> viewModel.removeProduct(productId) },
                        onUpdateQuantity = { productId, qty -> viewModel.updateProductQuantity(productId, qty) },
                        onUpdateNotes = { viewModel.updateNotes(it) },
                        onSave = { viewModel.saveDelivery() }
                    )
                }
            }
        }
    }
}
